package hr.example.assistant;

import java.util.Map;

/**
 * Represents an action the assistant wants to perform.
 */
public record AssistantAction(
        String action,           // "chat", "confirm_send", "send_to_agent", "lookup_contact", "list_agents", "help"
        Long agentId,            // Target agent for send_to_agent/confirm_send action
        String agentName,        // Agent name for display
        String skillId,          // Skill to invoke on agent
        Map<String, Object> data, // Data to send to agent
        String message           // Message to display to user
) {
    public static AssistantAction chat(String message) {
        return new AssistantAction("chat", null, null, null, Map.of(), message);
    }

    /**
     * Request confirmation before sending to agent.
     * This shows the data to the user for approval.
     */
    public static AssistantAction confirmSend(Long agentId, String agentName, String skillId, Map<String, Object> data, String message) {
        return new AssistantAction("confirm_send", agentId, agentName, skillId, data, message);
    }

    public static AssistantAction sendToAgent(Long agentId, String skillId, Map<String, Object> data, String message) {
        return new AssistantAction("send_to_agent", agentId, null, skillId, data, message);
    }

    public static AssistantAction lookupContact(String contactId, String message) {
        return new AssistantAction("lookup_contact", null, null, null, Map.of("contactId", contactId), message);
    }

    public static AssistantAction listAgents(String message) {
        return new AssistantAction("list_agents", null, null, null, Map.of(), message);
    }

    public static AssistantAction help(String message) {
        return new AssistantAction("help", null, null, null, Map.of(), message);
    }
}

