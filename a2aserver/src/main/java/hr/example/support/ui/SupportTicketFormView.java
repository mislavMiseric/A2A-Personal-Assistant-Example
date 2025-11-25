package hr.example.support.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import hr.example.base.ui.component.ViewToolbar;
import hr.example.support.SupportTicket;
import hr.example.support.SupportTicketService;

import java.util.List;
import java.util.Map;

@Route("support")
@PageTitle("Support Ticket")
@Menu(order = 3, icon = "vaadin:ticket", title = "Support Ticket")
public class SupportTicketFormView extends Main implements BeforeEnterObserver {

    private boolean autoSubmit = false;

    private final SupportTicketService ticketService;

    private final TextField subject;
    private final TextArea description;
    private final TextField reporterName;
    private final EmailField reporterEmail;
    private final ComboBox<SupportTicket.Priority> priority;
    private final ComboBox<SupportTicket.Category> category;
    private final Button submitBtn;

    public SupportTicketFormView(SupportTicketService ticketService) {
        this.ticketService = ticketService;

        subject = new TextField("Subject");
        subject.setRequired(true);
        subject.setId("ticket-subject");

        description = new TextArea("Description");
        description.setRequired(true);
        description.setMaxLength(4000);
        description.setHeight("200px");
        description.setId("ticket-description");

        reporterName = new TextField("Your Name");
        reporterName.setRequired(true);
        reporterName.setId("ticket-reporterName");

        reporterEmail = new EmailField("Your Email");
        reporterEmail.setRequired(true);
        reporterEmail.setId("ticket-reporterEmail");

        priority = new ComboBox<>("Priority");
        priority.setItems(SupportTicket.Priority.values());
        priority.setItemLabelGenerator(p -> p.name().charAt(0) + p.name().substring(1).toLowerCase());
        priority.setId("ticket-priority");

        category = new ComboBox<>("Category");
        category.setItems(SupportTicket.Category.values());
        category.setItemLabelGenerator(c -> c.name().replace("_", " "));
        category.setId("ticket-category");

        submitBtn = new Button("Submit Ticket", event -> submitForm());
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.setId("ticket-submit");

        FormLayout formLayout = new FormLayout();
        formLayout.add(subject, category, reporterName, reporterEmail, priority, description, submitBtn);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(subject, 2);
        formLayout.setColspan(description, 2);
        formLayout.setColspan(submitBtn, 2);

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Support Ticket"));
        add(formLayout);
    }

    private void submitForm() {
        if (subject.isEmpty() || description.isEmpty() || reporterName.isEmpty() || reporterEmail.isEmpty()) {
            Notification.show("Please fill in all required fields", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        ticketService.createTicket(
                subject.getValue(),
                description.getValue(),
                reporterName.getValue(),
                reporterEmail.getValue(),
                priority.getValue(),
                category.getValue()
        );

        clearForm();
        Notification.show("Support ticket submitted successfully!", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void clearForm() {
        subject.clear();
        description.clear();
        reporterName.clear();
        reporterEmail.clear();
        priority.clear();
        category.clear();
    }

    // Public methods for AI assistant to populate form
    public void setSubject(String value) {
        subject.setValue(value);
    }

    public void setDescription(String value) {
        description.setValue(value);
    }

    public void setReporterName(String value) {
        reporterName.setValue(value);
    }

    public void setReporterEmail(String value) {
        reporterEmail.setValue(value);
    }

    public void setPriority(SupportTicket.Priority value) {
        priority.setValue(value);
    }

    public void setCategory(SupportTicket.Category value) {
        category.setValue(value);
    }

    public void submit() {
        submitForm();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        QueryParameters params = event.getLocation().getQueryParameters();
        Map<String, List<String>> parameters = params.getParameters();

        parameters.getOrDefault("subject", List.of()).stream().findFirst().ifPresent(this::setSubject);
        parameters.getOrDefault("description", List.of()).stream().findFirst().ifPresent(this::setDescription);
        parameters.getOrDefault("reporterName", List.of()).stream().findFirst().ifPresent(this::setReporterName);
        parameters.getOrDefault("reporterEmail", List.of()).stream().findFirst().ifPresent(this::setReporterEmail);
        parameters.getOrDefault("priority", List.of()).stream().findFirst().ifPresent(priorityStr -> {
            try {
                setPriority(SupportTicket.Priority.valueOf(priorityStr.toUpperCase()));
            } catch (Exception ignored) {}
        });
        parameters.getOrDefault("category", List.of()).stream().findFirst().ifPresent(categoryStr -> {
            try {
                setCategory(SupportTicket.Category.valueOf(categoryStr.toUpperCase()));
            } catch (Exception ignored) {}
        });

        if (parameters.containsKey("_autoSubmit")) {
            autoSubmit = true;
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (autoSubmit) {
            autoSubmit = false;
            submitForm();
        }
    }
}

