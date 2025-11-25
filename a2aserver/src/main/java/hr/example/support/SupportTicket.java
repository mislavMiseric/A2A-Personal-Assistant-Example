package hr.example.support;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Entity
@Table(name = "support_ticket")
public class SupportTicket {

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Category {
        TECHNICAL, BILLING, GENERAL, FEATURE_REQUEST, BUG_REPORT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "ticket_id")
    private Long id;

    @Column(name = "subject", nullable = false)
    private String subject = "";

    @Column(name = "description", length = 4000, nullable = false)
    private String description = "";

    @Column(name = "reporter_name", nullable = false)
    private String reporterName = "";

    @Column(name = "reporter_email", nullable = false)
    private String reporterEmail = "";

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    @Nullable
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    @Nullable
    private Category category;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SupportTicket() {
    }

    public SupportTicket(String subject, String description, String reporterName, String reporterEmail) {
        this.subject = subject;
        this.description = description;
        this.reporterName = reporterName;
        this.reporterEmail = reporterEmail;
        this.createdAt = Instant.now();
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public @Nullable Priority getPriority() {
        return priority;
    }

    public void setPriority(@Nullable Priority priority) {
        this.priority = priority;
    }

    public @Nullable Category getCategory() {
        return category;
    }

    public void setCategory(@Nullable Category category) {
        this.category = category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        SupportTicket other = (SupportTicket) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

