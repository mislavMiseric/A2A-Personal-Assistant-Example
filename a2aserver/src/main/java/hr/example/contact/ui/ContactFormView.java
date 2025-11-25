package hr.example.contact.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import hr.example.contact.ContactService;

import java.util.List;
import java.util.Map;

@Route("contact")
@PageTitle("Contact Form")
@Menu(order = 1, icon = "vaadin:envelope", title = "Contact Form")
public class ContactFormView extends Main implements BeforeEnterObserver {

    private boolean autoSubmit = false;

    private final ContactService contactService;

    private final TextField firstName;
    private final TextField lastName;
    private final EmailField email;
    private final TextField phone;
    private final TextField company;
    private final TextArea message;
    private final Button submitBtn;

    public ContactFormView(ContactService contactService) {
        this.contactService = contactService;

        firstName = new TextField("First Name");
        firstName.setRequired(true);
        firstName.setId("contact-firstName");

        lastName = new TextField("Last Name");
        lastName.setRequired(true);
        lastName.setId("contact-lastName");

        email = new EmailField("Email");
        email.setRequired(true);
        email.setId("contact-email");

        phone = new TextField("Phone");
        phone.setId("contact-phone");

        company = new TextField("Company");
        company.setId("contact-company");

        message = new TextArea("Message");
        message.setMaxLength(2000);
        message.setHeight("150px");
        message.setId("contact-message");

        submitBtn = new Button("Submit", event -> submitForm());
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.setId("contact-submit");

        FormLayout formLayout = new FormLayout();
        formLayout.add(firstName, lastName, email, phone, company, message, submitBtn);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(message, 2);
        formLayout.setColspan(submitBtn, 2);

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Contact Form"));
        add(formLayout);
    }

    private void submitForm() {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Notification.show("Please fill in all required fields", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        contactService.createContact(
                firstName.getValue(),
                lastName.getValue(),
                email.getValue(),
                phone.getValue(),
                company.getValue(),
                message.getValue()
        );

        clearForm();
        Notification.show("Contact form submitted successfully!", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void clearForm() {
        firstName.clear();
        lastName.clear();
        email.clear();
        phone.clear();
        company.clear();
        message.clear();
    }

    // Public methods for AI assistant to populate form
    public void setFirstName(String value) {
        firstName.setValue(value);
    }

    public void setLastName(String value) {
        lastName.setValue(value);
    }

    public void setEmail(String value) {
        email.setValue(value);
    }

    public void setPhone(String value) {
        phone.setValue(value);
    }

    public void setCompany(String value) {
        company.setValue(value);
    }

    public void setMessage(String value) {
        message.setValue(value);
    }

    public void submit() {
        submitForm();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        QueryParameters params = event.getLocation().getQueryParameters();
        Map<String, List<String>> parameters = params.getParameters();

        parameters.getOrDefault("firstName", List.of()).stream().findFirst().ifPresent(this::setFirstName);
        parameters.getOrDefault("lastName", List.of()).stream().findFirst().ifPresent(this::setLastName);
        parameters.getOrDefault("email", List.of()).stream().findFirst().ifPresent(this::setEmail);
        parameters.getOrDefault("phone", List.of()).stream().findFirst().ifPresent(this::setPhone);
        parameters.getOrDefault("company", List.of()).stream().findFirst().ifPresent(this::setCompany);
        parameters.getOrDefault("message", List.of()).stream().findFirst().ifPresent(this::setMessage);

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

