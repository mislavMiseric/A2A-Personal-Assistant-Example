package hr.example.assistant;

/**
 * Represents a message in the chat history.
 */
public record ChatMessage(
        Role role,
        String content
) {
    public enum Role {
        USER, ASSISTANT, SYSTEM
    }

    public static ChatMessage user(String content) {
        return new ChatMessage(Role.USER, content);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage(Role.ASSISTANT, content);
    }

    public static ChatMessage system(String content) {
        return new ChatMessage(Role.SYSTEM, content);
    }
}

