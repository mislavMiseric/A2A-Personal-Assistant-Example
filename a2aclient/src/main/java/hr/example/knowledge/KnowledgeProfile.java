package hr.example.knowledge;

/**
 * Represents the owner's profile in the knowledge base.
 */
public record KnowledgeProfile(
        Owner owner,
        Preferences preferences
) {
    public record Owner(
            String firstName,
            String lastName,
            String email,
            String phone,
            String company,
            String position,
            String location,
            String timezone,
            String preferredLanguage,
            String bio
    ) {
        public String fullName() {
            return firstName + " " + lastName;
        }
    }

    public record Preferences(
            String communicationStyle,
            String defaultGreeting,
            String defaultSignature,
            WorkingHours workingHours
    ) {}

    public record WorkingHours(
            String start,
            String end,
            String timezone
    ) {}
    
    /**
     * Returns a formatted string for use in AI context.
     */
    public String toContextString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Owner Profile:\n");
        sb.append("  Name: ").append(owner.fullName()).append("\n");
        sb.append("  Email: ").append(owner.email()).append("\n");
        sb.append("  Phone: ").append(owner.phone()).append("\n");
        sb.append("  Company: ").append(owner.company()).append("\n");
        sb.append("  Position: ").append(owner.position()).append("\n");
        sb.append("  Location: ").append(owner.location()).append("\n");
        sb.append("  Bio: ").append(owner.bio()).append("\n");
        sb.append("\nPreferences:\n");
        sb.append("  Communication Style: ").append(preferences.communicationStyle()).append("\n");
        sb.append("  Default Greeting: ").append(preferences.defaultGreeting()).append("\n");
        sb.append("  Default Signature: ").append(preferences.defaultSignature()).append("\n");
        return sb.toString();
    }
}

