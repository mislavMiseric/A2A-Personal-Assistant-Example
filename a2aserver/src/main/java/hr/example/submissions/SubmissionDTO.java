package hr.example.submissions;

import java.time.Instant;
import java.util.Map;

/**
 * A unified DTO representing any form submission.
 */
public record SubmissionDTO(
        Long id,
        String formType,
        String title,
        String description,
        Instant createdAt,
        Map<String, String> details
) {
    public enum FormType {
        CONTACT("Contact"),
        EMPLOYEE("Employee"),
        SUPPORT("Support Ticket");

        private final String displayName;

        FormType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

