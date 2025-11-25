package hr.example.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.example.agent.AgentBookmark;
import hr.example.agent.AgentBookmarkService;
import hr.example.agent.a2a.A2AClientService;
import hr.example.knowledge.KnowledgeBaseService;
import hr.example.knowledge.KnowledgeContact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * AI-powered personal assistant service.
 * Uses knowledge base data to help users interact with A2A agent servers.
 */
@Service
public class AssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AssistantService.class);
    private static final int MAX_HISTORY_SIZE = 20;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final KnowledgeBaseService knowledgeBaseService;
    private final AgentBookmarkService agentBookmarkService;
    private final A2AClientService a2aClientService;

    public AssistantService(ChatClient.Builder chatClientBuilder,
                           ObjectMapper objectMapper,
                           KnowledgeBaseService knowledgeBaseService,
                           AgentBookmarkService agentBookmarkService,
                           A2AClientService a2aClientService) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.knowledgeBaseService = knowledgeBaseService;
        this.agentBookmarkService = agentBookmarkService;
        this.a2aClientService = a2aClientService;
    }

    /**
     * Process a user command and return an assistant action.
     */
    public AssistantAction processCommand(String userCommand, List<ChatMessage> history) {
        try {
            String systemPrompt = buildSystemPrompt();
            
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            
            // Add conversation history
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
            
            messages.add(new UserMessage(userCommand));
            
            Prompt prompt = new Prompt(messages);
            String response = chatClient.prompt(prompt)
                    .call()
                    .content();

            logger.info("AI Response: {}", response);
            return parseResponse(response);
        } catch (Exception e) {
            logger.error("Error processing command", e);
            return AssistantAction.help("I'm sorry, I encountered an error processing your request. Please try again.");
        }
    }

    /**
     * Execute an action that involves sending to an agent.
     * Handles both "send_to_agent" and "confirm_send" action types.
     */
    public Mono<String> executeAgentAction(AssistantAction action) {
        // Handle both confirm_send and send_to_agent actions
        if ((!("send_to_agent".equals(action.action()) || "confirm_send".equals(action.action()))) 
                || action.agentId() == null) {
            return Mono.just(action.message());
        }
        
        return a2aClientService.executeOnAgent(action.agentId(), action.skillId(), action.data())
                .map(response -> {
                    if (response.isSuccess()) {
                        return action.message() + "\n\n✅ Agent response: " + response.getMessage();
                    } else {
                        return action.message() + "\n\n❌ Agent error: " + 
                               (response.error() != null ? response.error().message() : "Unknown error");
                    }
                })
                .onErrorResume(e -> Mono.just(action.message() + "\n\n❌ Failed to contact agent: " + e.getMessage()));
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a personal AI assistant helping the user manage their contacts and interact with A2A agent servers.\n\n");
        
        sb.append("=== YOUR CAPABILITIES ===\n");
        sb.append("1. Access to a personal knowledge base with contacts, projects, and notes\n");
        sb.append("2. Ability to send tasks to A2A agent servers (like submitting forms)\n");
        sb.append("3. Look up contact information to use when interacting with agents\n\n");
        
        sb.append("=== KNOWLEDGE BASE ===\n");
        sb.append(knowledgeBaseService.getFullContext());
        sb.append("\n\n");
        
        sb.append("=== AVAILABLE AGENT SERVERS ===\n");
        sb.append(agentBookmarkService.getAgentsContext());
        sb.append("\n");
        
        // List available agents with IDs
        List<AgentBookmark> agents = agentBookmarkService.getActiveBookmarks();
        if (!agents.isEmpty()) {
            sb.append("\nAgent IDs for reference:\n");
            for (AgentBookmark agent : agents) {
                sb.append("- ID ").append(agent.getId()).append(": ").append(agent.getName());
                if (agent.getTag() != null) {
                    sb.append(" (tag: @").append(agent.getTag()).append(")");
                }
                sb.append("\n");
            }
        }
        
        sb.append("\n=== AVAILABLE SKILLS ON AGENTS ===\n");
        sb.append("- submit-contact: Submit a contact form (firstName, lastName, email, phone, company, message)\n");
        sb.append("- submit-employee: Submit an employee registration (firstName, lastName, email, department, position, hireDate, salary)\n");
        sb.append("- submit-support-ticket: Submit a support ticket (subject, description, reporterName, reporterEmail, priority, category)\n");
        sb.append("- ask-assistant: Send a natural language request to the agent's AI assistant\n");
        
        sb.append("\n=== RESPONSE FORMAT ===\n");
        sb.append("You must respond with a JSON object in one of these formats:\n\n");
        sb.append("1. For regular conversation:\n");
        sb.append("   {\"action\": \"chat\", \"message\": \"<your response>\"}\n\n");
        sb.append("2. To send data to an agent server (MUST include all data fields):\n");
        sb.append("   {\"action\": \"send_to_agent\", \"agentId\": <agent_id_number>, \"skillId\": \"<skill_id>\", \"data\": {\"firstName\": \"...\", \"lastName\": \"...\", ...}, \"message\": \"<explanation>\"}\n\n");
        sb.append("   EXAMPLE for submit-contact with Ante Antić:\n");
        sb.append("   {\"action\": \"send_to_agent\", \"agentId\": 1, \"skillId\": \"submit-contact\", \"data\": {\"firstName\": \"Ante\", \"lastName\": \"Antić\", \"email\": \"ante.antic@example.com\", \"phone\": \"+385 91 234 5678\", \"company\": \"Antić Solutions d.o.o.\"}, \"message\": \"I'll submit the contact form for Ante Antić.\"}\n\n");
        sb.append("3. To list available agents:\n");
        sb.append("   {\"action\": \"list_agents\", \"message\": \"<list of agents with descriptions>\"}\n\n");
        sb.append("4. For help or unclear requests:\n");
        sb.append("   {\"action\": \"help\", \"message\": \"<helpful explanation>\"}\n\n");
        
        sb.append("=== CRITICAL RULES ===\n");
        sb.append("- Always respond with valid JSON only, no additional text before or after the JSON\n");
        sb.append("- When user asks to submit a form, ALWAYS include the FULL DATA in the 'data' field\n");
        sb.append("- Look up contact information from the knowledge base and include ALL fields: firstName, lastName, email, phone, company\n");
        sb.append("- The 'data' field must contain actual values, NOT placeholders or references\n");
        sb.append("- Users can reference agents by name, tag (@FormAgent), or ID\n");
        sb.append("- For dates, use YYYY-MM-DD format\n");
        sb.append("- For priority values: LOW, MEDIUM, HIGH, CRITICAL\n");
        sb.append("- For category values: TECHNICAL, BILLING, GENERAL, FEATURE_REQUEST, BUG_REPORT\n");
        sb.append("- For department values: Engineering, Sales, Marketing, HR, Finance, Operations\n");
        sb.append("- Be helpful and always include complete data when submitting to agents\n");
        
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private AssistantAction parseResponse(String response) {
        try {
            String cleanResponse = cleanJsonResponse(response);
            logger.debug("Cleaned AI response: {}", cleanResponse);
            
            Map<String, Object> parsed = objectMapper.readValue(cleanResponse, Map.class);

            String action = (String) parsed.getOrDefault("action", "chat");
            String message = (String) parsed.getOrDefault("message", "");
            
            logger.info("Parsed action: {}, message length: {}", action, message.length());
            
            return switch (action) {
                case "send_to_agent" -> {
                    // Convert to confirm_send - always ask for approval before sending
                    Long agentId = parsed.get("agentId") != null 
                            ? ((Number) parsed.get("agentId")).longValue() 
                            : null;
                    String skillId = (String) parsed.get("skillId");
                    Map<String, Object> data = (Map<String, Object>) parsed.getOrDefault("data", Map.of());
                    
                    logger.info("send_to_agent: agentId={}, skillId={}, data keys={}", 
                            agentId, skillId, data != null ? data.keySet() : "null");
                    
                    // Get agent name for display
                    String agentName = agentId != null 
                            ? agentBookmarkService.getBookmarkById(agentId)
                                    .map(AgentBookmark::getName)
                                    .orElse("Unknown Agent")
                            : "Unknown Agent";
                    
                    yield AssistantAction.confirmSend(agentId, agentName, skillId, data, message);
                }
                case "list_agents" -> AssistantAction.listAgents(message);
                case "lookup_contact" -> {
                    String contactId = (String) ((Map<String, Object>) parsed.getOrDefault("data", Map.of())).get("contactId");
                    yield AssistantAction.lookupContact(contactId, message);
                }
                case "help" -> AssistantAction.help(message);
                default -> AssistantAction.chat(message);
            };
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse AI response: {}", response, e);
            return AssistantAction.help("I understood your request but had trouble formatting my response. Could you please rephrase?");
        }
    }

    private String cleanJsonResponse(String response) {
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
        return cleanResponse.trim();
    }

    /**
     * Get a contact by name for quick lookup.
     */
    public Optional<KnowledgeContact> findContact(String name) {
        return knowledgeBaseService.findContactByName(name);
    }

    /**
     * Get all contacts.
     */
    public List<KnowledgeContact> getAllContacts() {
        return knowledgeBaseService.getAllContacts();
    }

    /**
     * Get all active agents.
     */
    public List<AgentBookmark> getActiveAgents() {
        return agentBookmarkService.getActiveBookmarks();
    }
}

