package hr.example.assistant.ui;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import hr.example.agent.AgentBookmark;
import hr.example.assistant.AssistantAction;
import hr.example.assistant.AssistantService;
import hr.example.assistant.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main chat view for the Personal AI Assistant.
 */
@Route("")
@PageTitle("Personal AI Assistant")
@Menu(order = 0, icon = "vaadin:chat", title = "Assistant")
public class AssistantView extends VerticalLayout {

    private final AssistantService assistantService;
    private final VerticalLayout chatMessages;
    private final TextField inputField;
    private final ComboBox<AgentBookmark> agentSelector;
    private final List<ChatMessage> chatHistory = new ArrayList<>();

    public AssistantView(AssistantService assistantService) {
        this.assistantService = assistantService;
        
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        
        // Header
        add(createHeader());
        
        // Chat messages area
        chatMessages = new VerticalLayout();
        chatMessages.setWidthFull();
        chatMessages.setPadding(true);
        chatMessages.setSpacing(true);
        chatMessages.getStyle().set("padding-bottom", "100px");
        
        Scroller chatScroller = new Scroller(chatMessages);
        chatScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        chatScroller.setSizeFull();
        chatScroller.getStyle()
                .set("background", "var(--lumo-contrast-5pct)");
        
        add(chatScroller);
        setFlexGrow(1, chatScroller);
        
        // Agent selector
        agentSelector = new ComboBox<>();
        agentSelector.setPlaceholder("Select agent (optional)");
        agentSelector.setItems(assistantService.getActiveAgents());
        agentSelector.setItemLabelGenerator(agent -> {
            String label = agent.getName();
            if (agent.getTag() != null) {
                label += " @" + agent.getTag();
            }
            return label;
        });
        agentSelector.setClearButtonVisible(true);
        agentSelector.setWidth("250px");
        
        // Input area
        inputField = new TextField();
        inputField.setPlaceholder("Ask me anything... (e.g., 'Submit contact form for Ante Antić on @FormAgent agent')");
        inputField.setWidthFull();
        inputField.setClearButtonVisible(true);
        inputField.addClassName(LumoUtility.FontSize.MEDIUM);

        Button sendButton = new Button(VaadinIcon.PAPERPLANE.create(), e -> sendMessage());
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        // Use key press listener on the input field instead of global shortcut to prevent page refresh
        inputField.addKeyPressListener(Key.ENTER, e -> sendMessage());

        HorizontalLayout inputLayout = new HorizontalLayout(agentSelector, inputField, sendButton);
        inputLayout.setWidthFull();
        inputLayout.setFlexGrow(1, inputField);
        inputLayout.setPadding(true);
        inputLayout.setSpacing(true);
        inputLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        inputLayout.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-top", "1px solid var(--lumo-contrast-10pct)")
                .set("position", "sticky")
                .set("bottom", "0");
        
        add(inputLayout);
        
        // Add welcome message
        addWelcomeMessage();
    }

    private Div createHeader() {
        H2 title = new H2("🤖 Personal AI Assistant");
        title.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-primary-text-color)");
        
        Paragraph subtitle = new Paragraph("Your personal assistant with access to your contacts, projects, and agent servers");
        subtitle.addClassName(LumoUtility.TextColor.SECONDARY);
        subtitle.getStyle().set("margin", "4px 0 0 0");
        
        Div header = new Div(title, subtitle);
        header.getStyle()
                .set("padding", "var(--lumo-space-m) var(--lumo-space-l)")
                .set("background", "linear-gradient(135deg, var(--lumo-primary-color-10pct) 0%, var(--lumo-base-color) 100%)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        
        return header;
    }

    private void addWelcomeMessage() {
        String welcomeText = """
            👋 **Hello! I'm your Personal AI Assistant.**
            
            I have access to your knowledge base including:
            • **Contacts**: Your saved contacts with their details
            • **Projects**: Your active and past projects
            • **Notes**: Your personal notes and reminders
            
            I can also interact with A2A agent servers to:
            • Submit contact forms
            • Register employees
            • Create support tickets
            
            ⚠️ **Before submitting any data to an agent, I'll show you exactly what will be sent and ask for your approval.**
            
            **Try saying:**
            • "Show me my contacts"
            • "What agents are available?"
            • "Submit a contact form for Ante Antić on the FormAgent agent"
            • "Create a support ticket about login issues on @FormAgent agent"
            """;
        
        addAssistantMessage(welcomeText);
    }

    private void sendMessage() {
        String message = inputField.getValue().trim();
        if (message.isEmpty()) {
            return;
        }

        // Get UI reference immediately - try both methods for reliability
        UI ui = getUI().orElse(UI.getCurrent());
        if (ui == null) {
            addAssistantMessage("⚠️ Please wait a moment and try again.");
            return;
        }

        // Add agent context if selected
        AgentBookmark selectedAgent = agentSelector.getValue();
        final String contextualMessage;
        if (selectedAgent != null) {
            contextualMessage = "Use agent '" + selectedAgent.getName() + "' (ID: " + selectedAgent.getId() + ") for this request. " + message;
        } else {
            contextualMessage = message;
        }

        addUserMessage(message);
        inputField.clear();
        
        chatHistory.add(ChatMessage.user(contextualMessage));
        
        // Show typing indicator
        Div typingIndicator = createTypingIndicator();
        chatMessages.add(typingIndicator);
        scrollToBottom();
        
        List<ChatMessage> historyCopy = new ArrayList<>(chatHistory);
        
        new Thread(() -> {
            try {
                AssistantAction action = assistantService.processCommand(contextualMessage, historyCopy);
                
                ui.access(() -> {
                    chatMessages.remove(typingIndicator);
                    handleAction(action);
                    chatHistory.add(ChatMessage.assistant(action.message()));
                });
            } catch (Exception e) {
                ui.access(() -> {
                    chatMessages.remove(typingIndicator);
                    addAssistantMessage("❌ Sorry, I encountered an error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void handleAction(AssistantAction action) {
        switch (action.action()) {
            case "confirm_send" -> {
                // Show confirmation dialog with data preview before sending
                addAssistantMessage(action.message());
                showSubmissionConfirmationDialog(action);
            }
            case "send_to_agent" -> {
                // Direct send (shouldn't happen normally, but handle it)
                addAssistantMessage(action.message());
                showSubmissionConfirmationDialog(action);
            }
            case "list_agents" -> addAssistantMessage(action.message());
            case "lookup_contact" -> addAssistantMessage(action.message());
            case "help" -> addAssistantMessage("ℹ️ " + action.message());
            default -> addAssistantMessage(action.message());
        }
    }

    /**
     * Shows a confirmation dialog with the data that will be submitted to the agent.
     */
    private void showSubmissionConfirmationDialog(AssistantAction action) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("📋 Confirm Submission");
        dialog.setWidth("550px");
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        
        // Agent info - use agentName from action if available, otherwise look it up
        String agentName = action.agentName() != null 
                ? action.agentName()
                : assistantService.getActiveAgents().stream()
                        .filter(a -> a.getId().equals(action.agentId()))
                        .map(AgentBookmark::getName)
                        .findFirst()
                        .orElse("Agent #" + action.agentId());
        
        Div agentInfo = new Div();
        agentInfo.getStyle()
                .set("padding", "12px 16px")
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("border-radius", "8px")
                .set("margin-bottom", "8px");
        
        Span agentLabel = new Span("🎯 Target Agent: ");
        agentLabel.getStyle().set("font-weight", "bold");
        Span agentValue = new Span(agentName);
        agentInfo.add(agentLabel, agentValue);
        
        Div skillInfo = new Div();
        Span skillLabel = new Span("⚡ Action: ");
        skillLabel.getStyle().set("font-weight", "bold");
        Span skillValue = new Span(formatSkillName(action.skillId()));
        skillInfo.add(skillLabel, skillValue);
        skillInfo.getStyle().set("margin-top", "4px");
        agentInfo.add(new Div(skillInfo));
        
        content.add(agentInfo);
        
        // Data preview
        H4 dataTitle = new H4("📝 Data to be submitted:");
        dataTitle.getStyle().set("margin", "8px 0");
        content.add(dataTitle);
        
        Div dataContainer = new Div();
        dataContainer.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("max-height", "300px")
                .set("overflow-y", "auto");
        
        // Display each field
        if (action.data() != null && !action.data().isEmpty()) {
            for (Map.Entry<String, Object> entry : action.data().entrySet()) {
                // Skip internal fields starting with underscore
                if (!entry.getKey().startsWith("_")) {
                    Div fieldRow = createDataFieldRow(entry.getKey(), entry.getValue());
                    dataContainer.add(fieldRow);
                }
            }
        } else {
            Div noDataWarning = new Div();
            noDataWarning.getStyle()
                    .set("padding", "16px")
                    .set("background", "var(--lumo-error-color-10pct)")
                    .set("border-radius", "8px")
                    .set("text-align", "center");
            
            Paragraph noDataText = new Paragraph("⚠️ No data fields were extracted from your request.");
            noDataText.getStyle().set("margin", "0 0 8px 0").set("font-weight", "bold");
            
            Paragraph helpText = new Paragraph("Try being more specific, e.g.: 'Submit contact form for Ante Antić with his email and phone on @local'");
            helpText.addClassName(LumoUtility.TextColor.SECONDARY);
            helpText.getStyle().set("margin", "0").set("font-size", "var(--lumo-font-size-s)");
            
            noDataWarning.add(noDataText, helpText);
            dataContainer.add(noDataWarning);
        }
        
        content.add(dataContainer);
        
        // Warning message
        Div warning = new Div();
        warning.getStyle()
                .set("padding", "12px 16px")
                .set("background", "var(--lumo-warning-color-10pct)")
                .set("border-radius", "8px")
                .set("margin-top", "12px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "8px");
        
        Span warningIcon = new Span("⚠️");
        Span warningText = new Span("Please review the data above before confirming. This action will submit the data to the external agent server.");
        warningText.getStyle().set("font-size", "var(--lumo-font-size-s)");
        warning.add(warningIcon, warningText);
        content.add(warning);
        
        dialog.add(content);
        
        // Buttons
        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
            addSystemMessage("❌ Submission cancelled by user.");
            chatHistory.add(ChatMessage.assistant("User cancelled the submission."));
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        Button confirmButton = new Button("✓ Confirm & Submit", e -> {
            dialog.close();
            executeAgentSubmission(action);
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.getStyle().set("margin-left", "auto");
        
        HorizontalLayout buttons = new HorizontalLayout(cancelButton, confirmButton);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        dialog.getFooter().add(buttons);
        dialog.open();
    }

    /**
     * Creates a row displaying a data field and its value.
     */
    private Div createDataFieldRow(String key, Object value) {
        Div row = new Div();
        row.getStyle()
                .set("display", "flex")
                .set("padding", "6px 0")
                .set("border-bottom", "1px solid var(--lumo-contrast-5pct)");
        
        Span keySpan = new Span(formatFieldName(key) + ":");
        keySpan.getStyle()
                .set("font-weight", "500")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("min-width", "140px")
                .set("flex-shrink", "0");
        
        Span valueSpan = new Span(value != null ? value.toString() : "-");
        valueSpan.getStyle()
                .set("word-break", "break-word")
                .set("color", "var(--lumo-body-text-color)");
        
        row.add(keySpan, valueSpan);
        return row;
    }

    /**
     * Formats a skill ID into a readable name.
     */
    private String formatSkillName(String skillId) {
        if (skillId == null) return "Unknown";
        return switch (skillId) {
            case "submit-contact" -> "Submit Contact Form";
            case "submit-employee" -> "Register Employee";
            case "submit-support-ticket" -> "Create Support Ticket";
            case "ask-assistant" -> "Ask Agent Assistant";
            default -> skillId.replace("-", " ").replace("_", " ");
        };
    }

    /**
     * Formats a field name from camelCase to readable text.
     */
    private String formatFieldName(String fieldName) {
        if (fieldName == null) return "";
        // Add space before capital letters and capitalize first letter
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (i == 0) {
                result.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                result.append(" ").append(c);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Executes the agent submission after user confirmation.
     */
    private void executeAgentSubmission(AssistantAction action) {
        addSystemMessage("⏳ Sending to agent...");
        
        UI ui = UI.getCurrent();
        assistantService.executeAgentAction(action)
                .subscribe(result -> ui.access(() -> {
                    if (result.contains("✅")) {
                        addSystemMessage(result);
                        chatHistory.add(ChatMessage.assistant("Successfully submitted to agent."));
                    } else {
                        addSystemMessage("❌ " + result);
                        chatHistory.add(ChatMessage.assistant("Failed to submit to agent: " + result));
                    }
                }));
    }

    private Div createTypingIndicator() {
        Div indicator = new Div();
        indicator.setText("🤖 Thinking...");
        indicator.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.SMALL
        );
        indicator.getStyle()
                .set("font-style", "italic")
                .set("padding", "8px 16px");
        return indicator;
    }

    private void addUserMessage(String text) {
        Div messageDiv = createMessageDiv(text, true);
        chatMessages.add(messageDiv);
        scrollToBottom();
    }

    private void addAssistantMessage(String text) {
        Div messageDiv = createMessageDiv(text, false);
        chatMessages.add(messageDiv);
        scrollToBottom();
    }

    private void addSystemMessage(String text) {
        Paragraph systemMsg = new Paragraph(text);
        systemMsg.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextAlignment.CENTER
        );
        systemMsg.getStyle()
                .set("font-style", "italic")
                .set("margin", "8px 0");
        chatMessages.add(systemMsg);
        scrollToBottom();
    }

    private Div createMessageDiv(String text, boolean isUser) {
        Div wrapper = new Div();
        wrapper.getStyle()
                .set("display", "flex")
                .set("justify-content", isUser ? "flex-end" : "flex-start")
                .set("width", "100%");
        
        Div bubble = new Div();
        bubble.getStyle()
                .set("padding", "12px 16px")
                .set("border-radius", "16px")
                .set("max-width", "75%")
                .set("word-wrap", "break-word")
                .set("white-space", "pre-wrap");

        // Parse markdown-like formatting
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.startsWith("**") && line.endsWith("**") && line.length() > 4) {
                Span bold = new Span(line.substring(2, line.length() - 2));
                bold.getStyle().set("font-weight", "bold").set("display", "block");
                bubble.add(bold);
            } else if (line.contains("**")) {
                // Handle inline bold
                Div lineDiv = new Div();
                String[] parts = line.split("\\*\\*");
                for (int i = 0; i < parts.length; i++) {
                    Span span = new Span(parts[i]);
                    if (i % 2 == 1) {
                        span.getStyle().set("font-weight", "bold");
                    }
                    lineDiv.add(span);
                }
                bubble.add(lineDiv);
            } else if (line.startsWith("• ") || line.startsWith("- ")) {
                Paragraph p = new Paragraph(line);
                p.getStyle().set("margin", "4px 0").set("font-size", "14px");
                bubble.add(p);
            } else if (!line.isEmpty()) {
                Paragraph p = new Paragraph(line);
                p.getStyle().set("margin", "4px 0").set("font-size", "14px");
                bubble.add(p);
            } else {
                bubble.add(new Div()); // Empty line
            }
        }

        if (isUser) {
            bubble.getStyle()
                    .set("background", "var(--lumo-primary-color)")
                    .set("color", "white")
                    .set("border-bottom-right-radius", "4px");
        } else {
            bubble.getStyle()
                    .set("background", "var(--lumo-base-color)")
                    .set("border", "1px solid var(--lumo-contrast-10pct)")
                    .set("border-bottom-left-radius", "4px");
        }
        
        wrapper.add(bubble);
        return wrapper;
    }

    private void scrollToBottom() {
        chatMessages.getElement().executeJs(
                "setTimeout(() => { this.scrollIntoView({ behavior: 'smooth', block: 'end' }); }, 100);"
        );
    }
}

