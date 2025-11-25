package hr.example.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI-powered virtual assistant service that helps users navigate to forms
 * and populate them based on natural language requests.
 */
@Service
public class AssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AssistantService.class);
    private static final int MAX_HISTORY_SIZE = 20; // Keep last 20 messages for context

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AssistantService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Process a user command and return a navigation action (without history).
     */
    public NavigationAction processCommand(String userCommand) {
        return processCommand(userCommand, List.of());
    }

    /**
     * Process a user command with conversation history and return a navigation action.
     */
    public NavigationAction processCommand(String userCommand, List<ChatMessage> history) {
        try {
            String systemPrompt = buildSystemPrompt();
            
            // Build messages list with history
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            
            // Add conversation history (limit to recent messages)
            List<ChatMessage> recentHistory = history.size() > MAX_HISTORY_SIZE 
                    ? history.subList(history.size() - MAX_HISTORY_SIZE, history.size())
                    : history;
            
            for (ChatMessage msg : recentHistory) {
                switch (msg.role()) {
                    case USER -> messages.add(new UserMessage(msg.content()));
                    case ASSISTANT -> messages.add(new AssistantMessage(msg.content()));
                    case SYSTEM -> messages.add(new SystemMessage(msg.content()));
                }
            }
            
            // Add current user message
            messages.add(new UserMessage(userCommand));
            
            // Create prompt with all messages
            Prompt prompt = new Prompt(messages);
            String response = chatClient.prompt(prompt)
                    .call()
                    .content();

            logger.info("AI Response: {}", response);
            return parseResponse(response);
        } catch (Exception e) {
            logger.error("Error processing command", e);
            return NavigationAction.help("I'm sorry, I encountered an error processing your request. Please try again.");
        }
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a helpful virtual assistant for a web application called A2A Server. ");
        sb.append("Your job is to help users navigate to pages, fill out forms, and provide information.\n\n");
        
        sb.append("AVAILABLE PAGES:\n");
        sb.append("- submissions: Form Submissions page - view all submitted forms with filtering by type, date, search\n");
        sb.append("- (empty string): AI Assistant home page\n\n");
        
        sb.append("AVAILABLE FORMS:\n");

        for (FormInfo form : FormInfo.getAvailableForms()) {
            sb.append("\n- Form ID: ").append(form.formId());
            sb.append("\n  Name: ").append(form.displayName());
            sb.append("\n  Route: ").append(form.route());
            sb.append("\n  Description: ").append(form.description());
            sb.append("\n  Fields:\n");
            for (FormInfo.FieldInfo field : form.fields()) {
                sb.append("    - ").append(field.name())
                        .append(" (").append(field.type()).append(")")
                        .append(field.required() ? " [REQUIRED]" : "")
                        .append(": ").append(field.description()).append("\n");
            }
        }

        sb.append("\nRESPONSE FORMAT - You must respond with a JSON object in one of these formats:\n");
        sb.append("1. To navigate to a form or page: {\"action\": \"navigate\", \"formId\": \"<form_id or page_route>\", \"message\": \"<helpful message>\"}\n");
        sb.append("2. To navigate and populate a form: {\"action\": \"populate\", \"formId\": \"<form_id>\", \"formData\": {<field_name>: <value>, ...}, \"message\": \"<helpful message>\"}\n");
        sb.append("3. To submit a form with data: {\"action\": \"submit\", \"formId\": \"<form_id>\", \"formData\": {<field_name>: <value>, ...}, \"message\": \"<helpful message>\"}\n");
        sb.append("4. To list available forms: {\"action\": \"list_forms\", \"message\": \"<list of forms>\"}\n");
        sb.append("5. For help or unclear requests: {\"action\": \"help\", \"message\": \"<helpful explanation>\"}\n\n");
        
        sb.append("IMPORTANT RULES:\n");
        sb.append("- Always respond with valid JSON only, no additional text before or after\n");
        sb.append("- For dates, use YYYY-MM-DD format\n");
        sb.append("- For enum fields like priority, use exact values: LOW, MEDIUM, HIGH, CRITICAL\n");
        sb.append("- For category, use: TECHNICAL, BILLING, GENERAL, FEATURE_REQUEST, BUG_REPORT\n");
        sb.append("- For department, use: Engineering, Sales, Marketing, HR, Finance, Operations\n");
        sb.append("- Extract any data the user provides and include it in formData\n");
        sb.append("- If user wants to submit, use action 'submit'. If they just want to fill, use 'populate'\n");
        sb.append("- If user is on a form page (context provided), you can populate that form directly\n");
        sb.append("- For viewing submissions, navigate to 'submissions' route\n");

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private NavigationAction parseResponse(String response) {
        try {
            // Clean up the response - remove markdown code blocks if present
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.startsWith("```")) {
                cleanResponse = cleanResponse.substring(3);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();

            Map<String, Object> parsed = objectMapper.readValue(cleanResponse, Map.class);

            String action = (String) parsed.getOrDefault("action", "help");
            String formId = (String) parsed.get("formId");
            Map<String, Object> formData = (Map<String, Object>) parsed.getOrDefault("formData", Map.of());
            String message = (String) parsed.getOrDefault("message", "");

            return new NavigationAction(action, formId, formData, message);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse AI response: {}", response, e);
            return NavigationAction.help("I understood your request but had trouble formatting my response. Could you please rephrase?");
        }
    }

    /**
     * Get a description of all available forms.
     */
    public String getFormsDescription() {
        return FormInfo.getAvailableForms().stream()
                .map(f -> String.format("• %s (%s): %s", f.displayName(), f.route(), f.description()))
                .collect(Collectors.joining("\n"));
    }
}

