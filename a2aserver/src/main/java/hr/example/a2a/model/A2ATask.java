package hr.example.a2a.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a task in the A2A protocol.
 */
public class A2ATask {

    public enum Status {
        SUBMITTED, WORKING, INPUT_REQUIRED, COMPLETED, FAILED, CANCELED
    }

    private String id;
    private String sessionId;
    private Status status;
    private Map<String, Object> input;
    private A2AMessage result;
    private List<A2AArtifact> artifacts;
    private Instant createdAt;
    private Instant updatedAt;

    public A2ATask() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.status = Status.SUBMITTED;
        this.artifacts = List.of();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public void setInput(Map<String, Object> input) {
        this.input = input;
    }

    public A2AMessage getResult() {
        return result;
    }

    public void setResult(A2AMessage result) {
        this.result = result;
    }

    public List<A2AArtifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<A2AArtifact> artifacts) {
        this.artifacts = artifacts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public record A2AMessage(
            String role,
            List<A2APart> parts
    ) {
        public static A2AMessage agentMessage(String text) {
            return new A2AMessage("agent", List.of(new A2APart("text", text, null)));
        }
    }

    public record A2APart(
            String type,
            String text,
            Map<String, Object> data
    ) {}

    public record A2AArtifact(
            String name,
            String mimeType,
            Map<String, Object> data
    ) {}
}

