package hr.example.a2a;

import hr.example.a2a.model.A2ARequest;
import hr.example.a2a.model.A2AResponse;
import hr.example.a2a.model.A2ATask;
import hr.example.a2a.model.AgentCard;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller implementing the A2A (Agent-to-Agent) protocol.
 * 
 * Endpoints:
 * - GET /.well-known/agent.json - Returns the agent card (discovery)
 * - POST /a2a - JSON-RPC endpoint for task operations
 */
@RestController
public class A2AController {

    private static final Logger logger = LoggerFactory.getLogger(A2AController.class);

    private final A2AService a2aService;

    public A2AController(A2AService a2aService) {
        this.a2aService = a2aService;
    }

    /**
     * Agent Card endpoint - allows other agents to discover this agent's capabilities.
     */
    @GetMapping(value = "/.well-known/agent.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgentCard> getAgentCard(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        return ResponseEntity.ok(AgentCard.createDefault(baseUrl));
    }

    /**
     * Main A2A JSON-RPC endpoint.
     * Supports methods: tasks/send, tasks/get, tasks/cancel
     */
    @PostMapping(value = "/a2a", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<A2AResponse> handleA2ARequest(@RequestBody A2ARequest request) {
        logger.info("Received A2A request: method={}, id={}", request.method(), request.id());

        if (!"2.0".equals(request.jsonrpc())) {
            return ResponseEntity.ok(A2AResponse.error(request.id(), A2AResponse.INVALID_REQUEST, "Invalid JSON-RPC version"));
        }

        try {
            return switch (request.method()) {
                case "tasks/send" -> handleTasksSend(request);
                case "tasks/get" -> handleTasksGet(request);
                case "tasks/cancel" -> handleTasksCancel(request);
                default -> ResponseEntity.ok(A2AResponse.error(request.id(), A2AResponse.METHOD_NOT_FOUND, "Unknown method: " + request.method()));
            };
        } catch (Exception e) {
            logger.error("Error processing A2A request", e);
            return ResponseEntity.ok(A2AResponse.error(request.id(), A2AResponse.INTERNAL_ERROR, e.getMessage()));
        }
    }

    /**
     * Handle tasks/send - Create and execute a new task.
     */
    @SuppressWarnings("unchecked")
    private ResponseEntity<A2AResponse> handleTasksSend(A2ARequest request) {
        Map<String, Object> params = request.params();
        if (params == null) {
            return ResponseEntity.ok(A2AResponse.error(request.id(), A2AResponse.INVALID_PARAMS, "params is required"));
        }

        String skillId = (String) params.get("skill");
        if (skillId == null) {
            // Try to get from message content for natural language requests
            Map<String, Object> message = (Map<String, Object>) params.get("message");
            if (message != null && message.containsKey("parts")) {
                skillId = "ask-assistant";
                // Extract text from parts
                var parts = (java.util.List<Map<String, Object>>) message.get("parts");
                if (!parts.isEmpty()) {
                    String text = (String) parts.get(0).get("text");
                    params = Map.of("message", text);
                }
            } else {
                return ResponseEntity.ok(A2AResponse.error(request.id(), A2AResponse.INVALID_PARAMS, "skill or message is required"));
            }
        }

        Map<String, Object> input = (Map<String, Object>) params.getOrDefault("input", params);

        A2ATask task = a2aService.executeTask(skillId, input);

        return ResponseEntity.ok(A2AResponse.success(request.id(), Map.of(
                "id", task.getId(),
                "status", task.getStatus().name().toLowerCase(),
                "result", task.getResult() != null ? task.getResult() : Map.of(),
                "artifacts", task.getArtifacts()
        )));
    }

    /**
     * Handle tasks/get - Get the status of an existing task.
     */
    private ResponseEntity<A2AResponse> handleTasksGet(A2ARequest request) {
        Map<String, Object> params = request.params();
        if (params == null || !params.containsKey("id")) {
            return ResponseEntity.ok(A2AResponse.error(request.id(), A2AResponse.INVALID_PARAMS, "task id is required"));
        }

        String taskId = (String) params.get("id");
        A2ATask task = a2aService.getTask(taskId);

        if (task == null) {
            return ResponseEntity.ok(A2AResponse.error(request.id(), A2AResponse.TASK_NOT_FOUND, "Task not found: " + taskId));
        }

        return ResponseEntity.ok(A2AResponse.success(request.id(), Map.of(
                "id", task.getId(),
                "status", task.getStatus().name().toLowerCase(),
                "result", task.getResult() != null ? task.getResult() : Map.of(),
                "artifacts", task.getArtifacts()
        )));
    }

    /**
     * Handle tasks/cancel - Cancel a running task.
     */
    private ResponseEntity<A2AResponse> handleTasksCancel(A2ARequest request) {
        Map<String, Object> params = request.params();
        if (params == null || !params.containsKey("id")) {
            return ResponseEntity.ok(A2AResponse.error(request.id(), A2AResponse.INVALID_PARAMS, "task id is required"));
        }

        String taskId = (String) params.get("id");
        boolean canceled = a2aService.cancelTask(taskId);

        if (!canceled) {
            return ResponseEntity.ok(A2AResponse.error(request.id(), A2AResponse.TASK_NOT_FOUND, "Task not found or cannot be canceled: " + taskId));
        }

        return ResponseEntity.ok(A2AResponse.success(request.id(), Map.of(
                "id", taskId,
                "status", "canceled"
        )));
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if (("http".equals(scheme) && serverPort != 80) ||
                ("https".equals(scheme) && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        return url.toString();
    }
}

