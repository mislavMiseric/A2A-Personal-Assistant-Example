package hr.example.assistant.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import hr.example.assistant.AssistantService;
import hr.example.assistant.ChatMessage;
import hr.example.assistant.FormInfo;
import hr.example.assistant.NavigationAction;
import hr.example.base.ui.component.ViewToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Route("")
@PageTitle("AI Assistant")
@Menu(order = 0, icon = "vaadin:magic", title = "AI Assistant")
public class AssistantView extends Main {

    private final AssistantService assistantService;
    private final VerticalLayout chatMessages;
    private final TextField inputField;
    private final List<ChatMessage> chatHistory = new ArrayList<>();

    public AssistantView(AssistantService assistantService) {
        this.assistantService = assistantService;

        chatMessages = new VerticalLayout();
        chatMessages.setSpacing(true);
        chatMessages.setPadding(true);
        chatMessages.setWidthFull();

        Scroller chatScroller = new Scroller(chatMessages);
        chatScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        chatScroller.setSizeFull();
        chatScroller.addClassName(LumoUtility.Background.CONTRAST_5);
        chatScroller.getStyle().set("border-radius", "var(--lumo-border-radius-l)");

        inputField = new TextField();
        inputField.setPlaceholder("Ask me to navigate to a form or help you fill one out...");
        inputField.setWidthFull();
        inputField.setClearButtonVisible(true);

        Button sendButton = new Button(VaadinIcon.PAPERPLANE.create(), e -> sendMessage());
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        // Use key press listener on the input field instead of global shortcut to prevent page refresh
        inputField.addKeyPressListener(Key.ENTER, e -> sendMessage());

        HorizontalLayout inputLayout = new HorizontalLayout(inputField, sendButton);
        inputLayout.setWidthFull();
        inputLayout.setFlexGrow(1, inputField);
        inputLayout.setPadding(false);

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("AI Virtual Assistant"));
        add(chatScroller);
        add(inputLayout);

        // Make the chat scroller grow to fill available space
        chatScroller.getStyle().set("flex-grow", "1");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        addAssistantMessage("""
                👋 Hello! I'm your AI assistant. I can help you:
                
                • Navigate to different forms in the application
                • Fill out forms with data you provide
                • Submit forms on your behalf
                
                **Available forms:**
                """ + assistantService.getFormsDescription() + """
                
                
                Try saying something like:
                - "Take me to the contact form"
                - "I want to submit a support ticket about a login issue"
                - "Register a new employee named John Smith in Engineering"
                """);
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

        // Add to chat history
        chatHistory.add(ChatMessage.user(message));

        // Show typing indicator
        Paragraph typingIndicator = new Paragraph("🤖 Thinking...");
        typingIndicator.getStyle().set("font-style", "italic").set("color", "var(--lumo-secondary-text-color)");
        chatMessages.add(typingIndicator);
        scrollToBottom();

        // Create a copy of history for thread safety
        List<ChatMessage> historyCopy = new ArrayList<>(chatHistory);
        
        new Thread(() -> {
            try {
                NavigationAction action = assistantService.processCommand(message, historyCopy);
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
                        addSystemMessage("Opening " + form.displayName() + " with your data...");
                        navigateWithData(form.route(), action.formData());
                    }
                }
            }
            case "submit" -> {
                if (action.formId() != null && action.formData() != null) {
                    FormInfo form = FormInfo.getFormById(action.formId());
                    if (form != null) {
                        addSystemMessage("Opening " + form.displayName() + " with your data (ready to submit)...");
                        Map<String, Object> dataWithSubmit = new java.util.HashMap<>(action.formData());
                        dataWithSubmit.put("_autoSubmit", "true");
                        navigateWithData(form.route(), dataWithSubmit);
                    }
                }
            }
            case "list_forms" -> {
                // Message already added above
            }
            case "help" -> {
                // Message already added above
            }
        }
    }

    private void navigateWithData(String route, Map<String, Object> data) {
        // Convert data to query parameters for passing to the form
        Map<String, String> params = new java.util.HashMap<>();
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
        bubble.addClassNames(
                LumoUtility.Padding.MEDIUM,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Margin.Bottom.SMALL
        );

        // Parse markdown-style formatting
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.startsWith("**") && line.endsWith("**")) {
                Span bold = new Span(line.substring(2, line.length() - 2));
                bold.getStyle().set("font-weight", "bold");
                bubble.add(bold);
            } else if (line.startsWith("• ") || line.startsWith("- ")) {
                Paragraph p = new Paragraph(line);
                p.getStyle().set("margin", "2px 0");
                bubble.add(p);
            } else if (!line.isEmpty()) {
                Paragraph p = new Paragraph(line);
                p.getStyle().set("margin", "4px 0");
                bubble.add(p);
            }
        }

        if (isUser) {
            bubble.addClassNames(LumoUtility.Background.PRIMARY);
            bubble.getStyle().set("color", "var(--lumo-primary-contrast-color)");
            bubble.getStyle().set("margin-left", "auto");
            bubble.getStyle().set("max-width", "80%");
        } else {
            bubble.addClassNames(LumoUtility.Background.CONTRAST_10);
            bubble.getStyle().set("margin-right", "auto");
            bubble.getStyle().set("max-width", "80%");
        }

        return bubble;
    }

    private void scrollToBottom() {
        chatMessages.getElement().executeJs(
                "setTimeout(() => { this.scrollTop = this.scrollHeight; }, 100);"
        );
    }
}

