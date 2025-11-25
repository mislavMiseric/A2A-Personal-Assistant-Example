package hr.example.knowledge;

import java.util.List;

/**
 * Represents a project in the knowledge base.
 */
public record KnowledgeProject(
        String id,
        String name,
        String description,
        String client,
        String contactPerson,
        String status,
        String startDate,
        String expectedEndDate,
        List<String> technologies,
        String budget,
        String notes
) {
    /**
     * Returns a formatted string for use in AI context.
     */
    public String toContextString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project: ").append(name).append("\n");
        sb.append("  Description: ").append(description).append("\n");
        sb.append("  Client: ").append(client).append("\n");
        sb.append("  Status: ").append(status).append("\n");
        sb.append("  Start Date: ").append(startDate).append("\n");
        sb.append("  Expected End: ").append(expectedEndDate).append("\n");
        if (technologies != null && !technologies.isEmpty()) {
            sb.append("  Technologies: ").append(String.join(", ", technologies)).append("\n");
        }
        if (budget != null) sb.append("  Budget: ").append(budget).append("\n");
        if (notes != null) sb.append("  Notes: ").append(notes).append("\n");
        return sb.toString();
    }
}

