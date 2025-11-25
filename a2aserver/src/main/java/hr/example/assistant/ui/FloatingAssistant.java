package hr.example.assistant.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import hr.example.assistant.AssistantService;
import hr.example.assistant.ChatMessage;
import hr.example.assistant.FormInfo;
import hr.example.assistant.NavigationAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A floating chat assistant that can be added to any page.
 * Provides AI-powered help for navigating forms and populating them.
 */
public class FloatingAssistant extends Div {

    private final AssistantService assistantService;
    private final VerticalLayout chatMessages;
    private final TextField inputField;
    private final Div chatPanel;
    private final Button toggleButton;
    private boolean isOpen = false;
    
    // Chat history for context
    private final List<ChatMessage> chatHistory = new ArrayList<>();
    
    // Callback for when the assistant wants to populate fields on current page
    private BiConsumer<String, Map<String, Object>> onPopulateFields;
    private String currentPageContext = "";

    public FloatingAssistant(AssistantService assistantService) {
        this.assistantService = assistantService;
        
        addClassName("floating-assistant");
        getStyle()
                .set("position", "fixed")
                .set("bottom", "20px")
                .set("right", "20px")
                .set("z-index", "1000")
                .set("pointer-events", "none"); // Don't block clicks on elements underneath

        // Toggle button (FAB)
        toggleButton = new Button(VaadinIcon.COMMENT_ELLIPSIS.create());
        toggleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        toggleButton.addClassName("assistant-fab");
        toggleButton.getStyle()
                .set("width", "56px")
                .set("height", "56px")
                .set("border-radius", "50%")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.25)")
                .set("cursor", "pointer");
        // Prevent any default form behavior and ensure button type
        toggleButton.getElement().setAttribute("type", "button");
        // Stop event propagation to prevent interference with other UI elements
        toggleButton.getElement().addEventListener("click", e -> {})
                .addEventData("event.stopPropagation()");
        toggleButton.addClickListener(e -> toggleChat());

        // Chat panel
        chatPanel = new Div();
        chatPanel.addClassName("assistant-chat-panel");
        chatPanel.getStyle()
                .set("display", "none")
                .set("width", "380px")
                .set("height", "500px")
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "16px")
                .set("box-shadow", "0 8px 32px rgba(0,0,0,0.2)")
                .set("margin-bottom", "12px")
                .set("overflow", "hidden")
                .set("flex-direction", "column");

        // Chat header
        Div header = new Div();
        header.getStyle()
                .set("background", "linear-gradient(135deg, var(--lumo-primary-color) 0%, var(--lumo-primary-color-50pct) 100%)")
                .set("padding", "16px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between");
        
        H3 title = new H3("🤖 AI Assistant");
        title.getStyle()
                .set("margin", "0")
                .set("color", "white")
                .set("font-size", "16px");
        
        Button closeBtn = new Button(VaadinIcon.CLOSE.create());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.getStyle().set("color", "white");
        closeBtn.addClickListener(e -> toggleChat());
        
        header.add(title, closeBtn);

        // Chat messages area
        chatMessages = new VerticalLayout();
        chatMessages.setSpacing(true);
        chatMessages.setPadding(true);
        chatMessages.setWidthFull();
        chatMessages.getStyle().set("min-height", "0");

        Scroller chatScroller = new Scroller(chatMessages);
        chatScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        chatScroller.getStyle()
                .set("flex-grow", "1")
                .set("background", "var(--lumo-contrast-5pct)");

        // Input area
        inputField = new TextField();
        inputField.setPlaceholder("Ask me anything...");
        inputField.setWidthFull();
        inputField.setClearButtonVisible(true);

        Button sendButton = new Button(VaadinIcon.PAPERPLANE.create(), e -> sendMessage());
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        // Use key press listener on the input field instead of global shortcut to prevent page refresh
        inputField.addKeyPressListener(Key.ENTER, e -> sendMessage());

        HorizontalLayout inputLayout = new HorizontalLayout(inputField, sendButton);
        inputLayout.setWidthFull();
        inputLayout.setFlexGrow(1, inputField);
        inputLayout.setPadding(true);
        inputLayout.getStyle().set("background", "var(--lumo-base-color)");

        // Assemble chat panel
        VerticalLayout chatContent = new VerticalLayout(header, chatScroller, inputLayout);
        chatContent.setPadding(false);
        chatContent.setSpacing(false);
        chatContent.setSizeFull();
        chatContent.getStyle().set("display", "flex").set("flex-direction", "column");
        chatScroller.getStyle().set("flex", "1 1 auto").set("min-height", "0");
        
        chatPanel.add(chatContent);

        // Add components - ensure container doesn't block clicks on other elements
        VerticalLayout container = new VerticalLayout(chatPanel, toggleButton);
        container.setPadding(false);
        container.setSpacing(false);
        container.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
        container.getStyle().set("pointer-events", "none"); // Container doesn't capture clicks
        
        // Only the actual interactive elements should capture clicks
        chatPanel.getStyle().set("pointer-events", "auto");
        toggleButton.getStyle().set("pointer-events", "auto");
        
        add(container);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Add welcome message
        if (chatMessages.getComponentCount() == 0) {
            addAssistantMessage("👋 Hi! I'm here to help. I can see you're on this page and can help you fill out forms or navigate anywhere. What would you like to do?");
        }
    }

    private void toggleChat() {
        isOpen = !isOpen;
        if (isOpen) {
            chatPanel.getStyle().set("display", "flex");
            toggleButton.setIcon(VaadinIcon.CLOSE.create());
            inputField.focus();
        } else {
            chatPanel.getStyle().set("display", "none");
            toggleButton.setIcon(VaadinIcon.COMMENT_ELLIPSIS.create());
        }
    }

    public void setPageContext(String context) {
        this.currentPageContext = context;
    }
    
    public void setOnPopulateFields(BiConsumer<String, Map<String, Object>> callback) {
        this.onPopulateFields = callback;
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

        addUserMessage(message);
        inputField.clear();

        // Add context about current page
        final String contextualMessage;
        if (!currentPageContext.isEmpty()) {
            contextualMessage = "Context: User is currently on " + currentPageContext + " page. " + message;
        } else {
            contextualMessage = message;
        }

        // Add to chat history
        chatHistory.add(ChatMessage.user(contextualMessage));

        // Show typing indicator
        Paragraph typingIndicator = new Paragraph("🤖 Thinking...");
        typingIndicator.getStyle().set("font-style", "italic").set("color", "var(--lumo-secondary-text-color)");
        chatMessages.add(typingIndicator);
        scrollToBottom();

        // Create a copy of history for thread safety
        List<ChatMessage> historyCopy = new ArrayList<>(chatHistory);
        
        new Thread(() -> {
            try {
                NavigationAction action = assistantService.processCommand(contextualMessage, historyCopy);
                ui.access(() -> {
                    chatMessages.remove(typingIndicator);
                    handleAction(action);
                    // Add assistant response to history
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

    private void handleAction(NavigationAction action) {
        addAssistantMessage(action.message());

        switch (action.action()) {
            case "navigate" -> {
                if (action.formId() != null) {
                    FormInfo form = FormInfo.getFormById(action.formId());
                    if (form != null) {
                        addSystemMessage("Navigating to " + form.displayName() + "...");
                        UI.getCurrent().navigate(form.route());
                    } else {
                        // Handle generic page navigation (like "submissions")
                        addSystemMessage("Navigating...");
                        UI.getCurrent().navigate(action.formId());
                    }
                }
            }
            case "populate" -> {
                if (action.formId() != null && action.formData() != null) {
                    FormInfo form = FormInfo.getFormById(action.formId());
                    if (form != null) {
                        // Check if we're on the same page
                        if (currentPageContext.toLowerCase().contains(action.formId().toLowerCase())) {
                            // Populate current page
                            if (onPopulateFields != null) {
                                addSystemMessage("Filling in the form fields...");
                                onPopulateFields.accept(action.formId(), action.formData());
                            }
                        } else {
                            // Navigate to the form with data
                            addSystemMessage("Opening " + form.displayName() + " with your data...");
                            navigateWithData(form.route(), action.formData());
                        }
                    }
                }
            }
            case "submit" -> {
                if (action.formId() != null && action.formData() != null) {
                    FormInfo form = FormInfo.getFormById(action.formId());
                    if (form != null) {
                        addSystemMessage("Opening " + form.displayName() + " with your data...");
                        Map<String, Object> dataWithSubmit = new HashMap<>(action.formData());
                        dataWithSubmit.put("_autoSubmit", "true");
                        navigateWithData(form.route(), dataWithSubmit);
                    }
                }
            }
        }
    }

    private void navigateWithData(String route, Map<String, Object> data) {
        Map<String, String> params = new HashMap<>();
        data.forEach((key, value) -> {
            if (value != null) {
                params.put(key, value.toString());
            }
        });
        UI.getCurrent().navigate(route, QueryParameters.simple(params));
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
        systemMsg.getStyle().set("font-style", "italic");
        chatMessages.add(systemMsg);
        scrollToBottom();
    }

    private Div createMessageDiv(String text, boolean isUser) {
        Div bubble = new Div();
        bubble.getStyle()
                .set("padding", "10px 14px")
                .set("border-radius", "16px")
                .set("margin-bottom", "8px")
                .set("max-width", "85%")
                .set("word-wrap", "break-word");

        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.startsWith("**") && line.endsWith("**") && line.length() > 4) {
                Span bold = new Span(line.substring(2, line.length() - 2));
                bold.getStyle().set("font-weight", "bold").set("display", "block");
                bubble.add(bold);
            } else if (line.startsWith("• ") || line.startsWith("- ")) {
                Paragraph p = new Paragraph(line);
                p.getStyle().set("margin", "2px 0").set("font-size", "14px");
                bubble.add(p);
            } else if (!line.isEmpty()) {
                Paragraph p = new Paragraph(line);
                p.getStyle().set("margin", "4px 0").set("font-size", "14px");
                bubble.add(p);
            }
        }

        if (isUser) {
            bubble.getStyle()
                    .set("background", "var(--lumo-primary-color)")
                    .set("color", "white")
                    .set("margin-left", "auto")
                    .set("border-bottom-right-radius", "4px");
        } else {
            bubble.getStyle()
                    .set("background", "var(--lumo-contrast-10pct)")
                    .set("margin-right", "auto")
                    .set("border-bottom-left-radius", "4px");
        }

        return bubble;
    }

    private void scrollToBottom() {
        chatMessages.getElement().executeJs(
                "setTimeout(() => { this.scrollTop = this.scrollHeight; }, 100);"
        );
    }
}

