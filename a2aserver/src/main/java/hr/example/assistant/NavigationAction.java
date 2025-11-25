package hr.example.assistant;

import java.util.Map;

/**
 * Represents an action to navigate to a form and optionally populate it.
 */
public record NavigationAction(
        String action,           // "navigate", "populate", "submit", "help", "list_forms"
        String formId,           // The form to navigate to
        Map<String, Object> formData,  // Data to populate the form with
        String message           // Message to display to the user
) {
    public static NavigationAction navigate(String formId, String message) {
        return new NavigationAction("navigate", formId, Map.of(), message);
    }

    public static NavigationAction populate(String formId, Map<String, Object> data, String message) {
        return new NavigationAction("populate", formId, data, message);
    }

    public static NavigationAction submit(String formId, Map<String, Object> data, String message) {
        return new NavigationAction("submit", formId, data, message);
    }

    public static NavigationAction help(String message) {
        return new NavigationAction("help", null, Map.of(), message);
    }

    public static NavigationAction listForms(String message) {
        return new NavigationAction("list_forms", null, Map.of(), message);
    }
}

