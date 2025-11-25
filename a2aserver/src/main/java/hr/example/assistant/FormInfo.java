package hr.example.assistant;

import java.util.List;
import java.util.Map;

/**
 * Represents information about available forms in the application.
 */
public record FormInfo(
        String formId,
        String displayName,
        String description,
        String route,
        List<FieldInfo> fields
) {
    public record FieldInfo(
            String name,
            String type,
            String description,
            boolean required
    ) {}

    public static List<FormInfo> getAvailableForms() {
        return List.of(
                new FormInfo(
                        "contact",
                        "Contact Form",
                        "Submit a contact inquiry or message",
                        "contact",
                        List.of(
                                new FieldInfo("firstName", "text", "First name of the contact", true),
                                new FieldInfo("lastName", "text", "Last name of the contact", true),
                                new FieldInfo("email", "email", "Email address", true),
                                new FieldInfo("phone", "text", "Phone number", false),
                                new FieldInfo("company", "text", "Company name", false),
                                new FieldInfo("message", "textarea", "Message content", false)
                        )
                ),
                new FormInfo(
                        "employee",
                        "Employee Registration",
                        "Register a new employee in the system",
                        "employee",
                        List.of(
                                new FieldInfo("firstName", "text", "First name of the employee", true),
                                new FieldInfo("lastName", "text", "Last name of the employee", true),
                                new FieldInfo("email", "email", "Work email address", true),
                                new FieldInfo("department", "select", "Department (Engineering, Sales, Marketing, HR, Finance, Operations)", false),
                                new FieldInfo("position", "text", "Job position/title", false),
                                new FieldInfo("hireDate", "date", "Hire date (YYYY-MM-DD format)", false),
                                new FieldInfo("salary", "number", "Annual salary", false)
                        )
                ),
                new FormInfo(
                        "support",
                        "Support Ticket",
                        "Submit a support ticket or bug report",
                        "support",
                        List.of(
                                new FieldInfo("subject", "text", "Ticket subject/title", true),
                                new FieldInfo("description", "textarea", "Detailed description of the issue", true),
                                new FieldInfo("reporterName", "text", "Your name", true),
                                new FieldInfo("reporterEmail", "email", "Your email address", true),
                                new FieldInfo("priority", "select", "Priority level (LOW, MEDIUM, HIGH, CRITICAL)", false),
                                new FieldInfo("category", "select", "Category (TECHNICAL, BILLING, GENERAL, FEATURE_REQUEST, BUG_REPORT)", false)
                        )
                )
        );
    }

    public static FormInfo getFormById(String formId) {
        return getAvailableForms().stream()
                .filter(f -> f.formId().equals(formId))
                .findFirst()
                .orElse(null);
    }
}

