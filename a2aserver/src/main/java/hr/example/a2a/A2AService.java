package hr.example.a2a;

import hr.example.a2a.model.A2ATask;
import hr.example.assistant.AssistantService;
import hr.example.assistant.NavigationAction;
import hr.example.contact.Contact;
import hr.example.contact.ContactService;
import hr.example.employee.Employee;
import hr.example.employee.EmployeeService;
import hr.example.support.SupportTicket;
import hr.example.support.SupportTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that handles A2A protocol task execution.
 */
@Service
public class A2AService {

    private static final Logger logger = LoggerFactory.getLogger(A2AService.class);

    private final AssistantService assistantService;
    private final ContactService contactService;
    private final EmployeeService employeeService;
    private final SupportTicketService supportTicketService;

    // In-memory task storage (in production, use a database)
    private final Map<String, A2ATask> tasks = new ConcurrentHashMap<>();

    public A2AService(AssistantService assistantService,
                      ContactService contactService,
                      EmployeeService employeeService,
                      SupportTicketService supportTicketService) {
        this.assistantService = assistantService;
        this.contactService = contactService;
        this.employeeService = employeeService;
        this.supportTicketService = supportTicketService;
    }

    /**
     * Execute a task based on the skill/method and input parameters.
     */
    public A2ATask executeTask(String skillId, Map<String, Object> input) {
        A2ATask task = new A2ATask();
        task.setInput(input);
        task.setStatus(A2ATask.Status.WORKING);
        tasks.put(task.getId(), task);

        try {
            Object result = switch (skillId) {
                case "navigate-form" -> handleNavigateForm(input);
                case "submit-contact" -> handleSubmitContact(input);
                case "submit-employee" -> handleSubmitEmployee(input);
                case "submit-support-ticket" -> handleSubmitSupportTicket(input);
                case "ask-assistant" -> handleAskAssistant(input);
                default -> throw new IllegalArgumentException("Unknown skill: " + skillId);
            };

            task.setStatus(A2ATask.Status.COMPLETED);
            task.setResult(A2ATask.A2AMessage.agentMessage(result.toString()));
            task.setArtifacts(List.of(new A2ATask.A2AArtifact(
                    "result",
                    "application/json",
                    result instanceof Map ? (Map<String, Object>) result : Map.of("data", result)
            )));

        } catch (Exception e) {
            logger.error("Task execution failed", e);
            task.setStatus(A2ATask.Status.FAILED);
            task.setResult(A2ATask.A2AMessage.agentMessage("Error: " + e.getMessage()));
        }

        return task;
    }

    /**
     * Get a task by ID.
     */
    public A2ATask getTask(String taskId) {
        return tasks.get(taskId);
    }

    /**
     * Cancel a task.
     */
    public boolean cancelTask(String taskId) {
        A2ATask task = tasks.get(taskId);
        if (task != null && task.getStatus() == A2ATask.Status.WORKING) {
            task.setStatus(A2ATask.Status.CANCELED);
            return true;
        }
        return false;
    }

    private Map<String, Object> handleNavigateForm(Map<String, Object> input) {
        String formId = (String) input.get("formId");
        if (formId == null) {
            throw new IllegalArgumentException("formId is required");
        }

        String route = switch (formId) {
            case "contact" -> "/contact";
            case "employee" -> "/employee";
            case "support" -> "/support";
            default -> throw new IllegalArgumentException("Unknown form: " + formId);
        };

        return Map.of(
                "success", true,
                "formId", formId,
                "route", route,
                "message", "Navigation to " + formId + " form ready. Route: " + route
        );
    }

    private Map<String, Object> handleSubmitContact(Map<String, Object> input) {
        String firstName = getRequiredString(input, "firstName");
        String lastName = getRequiredString(input, "lastName");
        String email = getRequiredString(input, "email");
        String phone = (String) input.get("phone");
        String company = (String) input.get("company");
        String message = (String) input.get("message");

        Contact contact = contactService.createContact(firstName, lastName, email, phone, company, message);

        return Map.of(
                "success", true,
                "contactId", contact.getId(),
                "message", "Contact form submitted successfully for " + firstName + " " + lastName
        );
    }

    private Map<String, Object> handleSubmitEmployee(Map<String, Object> input) {
        String firstName = getRequiredString(input, "firstName");
        String lastName = getRequiredString(input, "lastName");
        String email = getRequiredString(input, "email");
        String department = (String) input.get("department");
        String position = (String) input.get("position");

        LocalDate hireDate = null;
        if (input.get("hireDate") != null) {
            hireDate = LocalDate.parse(input.get("hireDate").toString());
        }

        Double salary = null;
        if (input.get("salary") != null) {
            salary = Double.parseDouble(input.get("salary").toString());
        }

        Employee employee = employeeService.createEmployee(firstName, lastName, email, department, position, hireDate, salary);

        return Map.of(
                "success", true,
                "employeeId", employee.getId(),
                "message", "Employee " + firstName + " " + lastName + " registered successfully"
        );
    }

    private Map<String, Object> handleSubmitSupportTicket(Map<String, Object> input) {
        String subject = getRequiredString(input, "subject");
        String description = getRequiredString(input, "description");
        String reporterName = getRequiredString(input, "reporterName");
        String reporterEmail = getRequiredString(input, "reporterEmail");

        SupportTicket.Priority priority = null;
        if (input.get("priority") != null) {
            priority = SupportTicket.Priority.valueOf(input.get("priority").toString().toUpperCase());
        }

        SupportTicket.Category category = null;
        if (input.get("category") != null) {
            category = SupportTicket.Category.valueOf(input.get("category").toString().toUpperCase());
        }

        SupportTicket ticket = supportTicketService.createTicket(subject, description, reporterName, reporterEmail, priority, category);

        return Map.of(
                "success", true,
                "ticketId", ticket.getId(),
                "message", "Support ticket created: " + subject
        );
    }

    private Map<String, Object> handleAskAssistant(Map<String, Object> input) {
        String message = getRequiredString(input, "message");
        NavigationAction action = assistantService.processCommand(message);

        return Map.of(
                "action", action.action(),
                "formId", action.formId() != null ? action.formId() : "",
                "formData", action.formData(),
                "message", action.message()
        );
    }

    private String getRequiredString(Map<String, Object> input, String key) {
        Object value = input.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value.toString();
    }
}

