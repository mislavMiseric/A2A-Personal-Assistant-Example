package hr.example.employee.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import hr.example.base.ui.component.ViewToolbar;
import hr.example.employee.EmployeeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Route("employee")
@PageTitle("Employee Registration")
@Menu(order = 2, icon = "vaadin:user", title = "Employee Form")
public class EmployeeFormView extends Main implements BeforeEnterObserver {

    private boolean autoSubmit = false;

    private final EmployeeService employeeService;

    private final TextField firstName;
    private final TextField lastName;
    private final EmailField email;
    private final ComboBox<String> department;
    private final TextField position;
    private final DatePicker hireDate;
    private final NumberField salary;
    private final Button submitBtn;

    public EmployeeFormView(EmployeeService employeeService) {
        this.employeeService = employeeService;

        firstName = new TextField("First Name");
        firstName.setRequired(true);
        firstName.setId("employee-firstName");

        lastName = new TextField("Last Name");
        lastName.setRequired(true);
        lastName.setId("employee-lastName");

        email = new EmailField("Email");
        email.setRequired(true);
        email.setId("employee-email");

        department = new ComboBox<>("Department");
        department.setItems("Engineering", "Sales", "Marketing", "HR", "Finance", "Operations");
        department.setId("employee-department");

        position = new TextField("Position");
        position.setId("employee-position");

        hireDate = new DatePicker("Hire Date");
        hireDate.setId("employee-hireDate");

        salary = new NumberField("Salary");
        salary.setPrefixComponent(new com.vaadin.flow.component.html.Span("$"));
        salary.setId("employee-salary");

        submitBtn = new Button("Register Employee", event -> submitForm());
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.setId("employee-submit");

        FormLayout formLayout = new FormLayout();
        formLayout.add(firstName, lastName, email, department, position, hireDate, salary, submitBtn);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(submitBtn, 2);

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Employee Registration"));
        add(formLayout);
    }

    private void submitForm() {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Notification.show("Please fill in all required fields", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        employeeService.createEmployee(
                firstName.getValue(),
                lastName.getValue(),
                email.getValue(),
                department.getValue(),
                position.getValue(),
                hireDate.getValue(),
                salary.getValue()
        );

        clearForm();
        Notification.show("Employee registered successfully!", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void clearForm() {
        firstName.clear();
        lastName.clear();
        email.clear();
        department.clear();
        position.clear();
        hireDate.clear();
        salary.clear();
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

    public void setDepartment(String value) {
        department.setValue(value);
    }

    public void setPosition(String value) {
        position.setValue(value);
    }

    public void setHireDate(LocalDate value) {
        hireDate.setValue(value);
    }

    public void setSalary(Double value) {
        salary.setValue(value);
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
        parameters.getOrDefault("department", List.of()).stream().findFirst().ifPresent(this::setDepartment);
        parameters.getOrDefault("position", List.of()).stream().findFirst().ifPresent(this::setPosition);
        parameters.getOrDefault("hireDate", List.of()).stream().findFirst().ifPresent(dateStr -> {
            try {
                setHireDate(LocalDate.parse(dateStr));
            } catch (Exception ignored) {}
        });
        parameters.getOrDefault("salary", List.of()).stream().findFirst().ifPresent(salaryStr -> {
            try {
                setSalary(Double.parseDouble(salaryStr));
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

