package hr.example.agent;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a bookmarked A2A agent server.
 * Users can save agent servers they want to interact with.
 */
@Entity
@Table(name = "agent_bookmarks")
public class AgentBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "agent_bookmark_seq")
    @SequenceGenerator(name = "agent_bookmark_seq", sequenceName = "agent_bookmark_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(length = 1000)
    private String description;

    @Column
    private String tag;

    @Column
    private boolean active = true;

    @Column(name = "agent_version")
    private String agentVersion;

    @Column(name = "last_connected")
    private Instant lastConnected;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public Instant getLastConnected() {
        return lastConnected;
    }

    public void setLastConnected(Instant lastConnected) {
        this.lastConnected = lastConnected;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentBookmark that = (AgentBookmark) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AgentBookmark{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", tag='" + tag + '\'' +
                ", active=" + active +
                '}';
    }
}

