package hr.example.a2a.model;

import java.util.List;
import java.util.Map;

/**
 * Agent Card that describes this agent's capabilities according to A2A protocol.
 * Other agents can discover this agent's capabilities by fetching this card.
 */
public record AgentCard(
        String name,
        String description,
        String url,
        String version,
        List<AgentSkill> skills,
        AgentCapabilities capabilities,
        Map<String, Object> authentication
) {
    public record AgentSkill(
            String id,
            String name,
            String description,
            List<String> tags,
            Map<String, Object> inputSchema,
            Map<String, Object> outputSchema
    ) {}

    public record AgentCapabilities(
            boolean streaming,
            boolean pushNotifications,
            boolean stateTransitionHistory
    ) {}

    public static AgentCard createDefault(String baseUrl) {
        return new AgentCard(
                "Form Assistant Agent",
                "An AI-powered agent that can navigate to forms, populate them with data, and submit them. " +
                        "Supports contact forms, employee registration, and support tickets.",
                baseUrl,
                "1.0.0",
                List.of(
                        new AgentSkill(
                                "navigate-form",
                                "Navigate to Form",
                                "Navigate to a specific form in the application",
                                List.of("navigation", "forms"),
                                Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "formId", Map.of("type", "string", "enum", List.of("contact", "employee", "support"))
                                        ),
                                        "required", List.of("formId")
                                ),
                                Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "success", Map.of("type", "boolean"),
                                                "message", Map.of("type", "string")
                                        )
                                )
                        ),
                        new AgentSkill(
                                "submit-contact",
                                "Submit Contact Form",
                                "Submit a contact form with the provided data",
                                List.of("forms", "contact", "submission"),
                                Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "firstName", Map.of("type", "string"),
                                                "lastName", Map.of("type", "string"),
                                                "email", Map.of("type", "string", "format", "email"),
                                                "phone", Map.of("type", "string"),
                                                "company", Map.of("type", "string"),
                                                "message", Map.of("type", "string")
                                        ),
                                        "required", List.of("firstName", "lastName", "email")
                                ),
                                Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "success", Map.of("type", "boolean"),
                                                "contactId", Map.of("type", "integer"),
                                                "message", Map.of("type", "string")
                                        )
                                )
                        ),
                        new AgentSkill(
                                "submit-employee",
                                "Submit Employee Registration",
                                "Register a new employee with the provided data",
                                List.of("forms", "employee", "registration"),
                                Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "firstName", Map.of("type", "string"),
                                                "lastName", Map.of("type", "string"),
                                                "email", Map.of("type", "string", "format", "email"),
                                                "department", Map.of("type", "string", "enum", List.of("Engineering", "Sales", "Marketing", "HR", "Finance", "Operations")),
                                                "position", Map.of("type", "string"),
                                                "hireDate", Map.of("type", "string", "format", "date"),
                                                "salary", Map.of("type", "number")
                                        ),
                                        "required", List.of("firstName", "lastName", "email")
                                ),
                                Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "success", Map.of("type", "boolean"),
                                                "employeeId", Map.of("type", "integer"),
                                                "message", Map.of("type", "string")
                                        )
                                )
                        ),
                        new AgentSkill(
                                "submit-support-ticket",
                                "Submit Support Ticket",
                                "Create a new support ticket",
                                List.of("forms", "support", "ticket"),
                                Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "subject", Map.of("type", "string"),
                                                "description", Map.of("type", "string"),
                                                "reporterName", Map.of("type", "string"),
                                                "reporterEmail", Map.of("type", "string", "format", "email"),
                                                "priority", Map.of("type", "string", "enum", List.of("LOW", "MEDIUM", "HIGH", "CRITICAL")),
                                                "category", Map.of("type", "string", "enum", List.of("TECHNICAL", "BILLING", "GENERAL", "FEATURE_REQUEST", "BUG_REPORT"))
                                        ),
                                        "required", List.of("subject", "description", "reporterName", "reporterEmail")
                                ),
                                Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "success", Map.of("type", "boolean"),
                                                "ticketId", Map.of("type", "integer"),
                                                "message", Map.of("type", "string")
                                        )
                                )
                        ),
                        new AgentSkill(
                                "ask-assistant",
                                "Ask AI Assistant",
                                "Send a natural language request to the AI assistant to navigate or fill forms",
                                List.of("ai", "assistant", "natural-language"),
                                Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "message", Map.of("type", "string", "description", "Natural language request")
                                        ),
                                        "required", List.of("message")
                                ),
                                Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "action", Map.of("type", "string"),
                                                "formId", Map.of("type", "string"),
                                                "formData", Map.of("type", "object"),
                                                "message", Map.of("type", "string")
                                        )
                                )
                        )
                ),
                new AgentCapabilities(false, false, false),
                Map.of("schemes", List.of("none"))
        );
    }
}

