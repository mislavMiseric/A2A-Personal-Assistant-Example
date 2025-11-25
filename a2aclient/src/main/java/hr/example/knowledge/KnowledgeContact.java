package hr.example.knowledge;

import java.util.List;

/**
 * Represents a contact in the knowledge base.
 */
public record KnowledgeContact(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String company,
        String position,
        String address,
        String notes,
        List<String> tags
) {
    public String fullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Returns a formatted string for use in AI context.
     */
    public String toContextString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Contact: ").append(fullName()).append("\n");
        sb.append("  Email: ").append(email).append("\n");
        if (phone != null) sb.append("  Phone: ").append(phone).append("\n");
        if (company != null) sb.append("  Company: ").append(company).append("\n");
        if (position != null) sb.append("  Position: ").append(position).append("\n");
        if (address != null) sb.append("  Address: ").append(address).append("\n");
        if (notes != null) sb.append("  Notes: ").append(notes).append("\n");
        if (tags != null && !tags.isEmpty()) sb.append("  Tags: ").append(String.join(", ", tags)).append("\n");
        return sb.toString();
    }
}

