package hr.example.agent.a2a;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.example.agent.AgentBookmark;
import hr.example.agent.AgentBookmarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

/**
 * Service for communicating with A2A agent servers.
 * Implements the client side of the A2A protocol.
 */
@Service
public class A2AClientService {

    private static final Logger logger = LoggerFactory.getLogger(A2AClientService.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;
    private final AgentBookmarkService bookmarkService;
    private final ObjectMapper objectMapper;

    public A2AClientService(WebClient.Builder webClientBuilder, 
                           AgentBookmarkService bookmarkService,
                           ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.bookmarkService = bookmarkService;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches the agent card from an A2A server.
     */
    public Mono<AgentCard> fetchAgentCard(String baseUrl) {
        String url = baseUrl + "/.well-known/agent.json";
        logger.info("Fetching agent card from: {}", url);
        
        return webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AgentCard.class)
                .timeout(TIMEOUT)
                .doOnSuccess(card -> logger.info("Successfully fetched agent card: {}", card.name()))
                .doOnError(e -> logger.error("Failed to fetch agent card from {}: {}", url, e.getMessage()));
    }

    /**
     * Sends a task to an A2A agent server.
     */
    public Mono<A2AResponse> sendTask(String baseUrl, String skillId, Map<String, Object> input) {
        String url = baseUrl + "/a2a";
        
        Map<String, Object> params = new HashMap<>();
        params.put("skill", skillId);
        params.put("input", input);
        
        A2ARequest request = new A2ARequest(
                "2.0",
                "tasks/send",
                UUID.randomUUID().toString(),
                params
        );
        
        logger.info("Sending task to {}: skill={}, params={}", url, skillId, input);
        
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(A2AResponse.class)
                .timeout(TIMEOUT)
                .doOnSuccess(response -> logger.info("Received response: {}", response))
                .doOnError(e -> logger.error("Failed to send task to {}: {}", url, e.getMessage()));
    }

    /**
     * Sends a natural language message to an A2A agent's assistant.
     */
    public Mono<A2AResponse> askAgent(String baseUrl, String message) {
        return sendTask(baseUrl, "ask-assistant", Map.of("message", message));
    }

    /**
     * Sends a task to submit a contact form on an agent server.
     */
    public Mono<A2AResponse> submitContact(String baseUrl, Map<String, Object> contactData) {
        return sendTask(baseUrl, "submit-contact", contactData);
    }

    /**
     * Sends a task to submit an employee form on an agent server.
     */
    public Mono<A2AResponse> submitEmployee(String baseUrl, Map<String, Object> employeeData) {
        return sendTask(baseUrl, "submit-employee", employeeData);
    }

    /**
     * Sends a task to submit a support ticket on an agent server.
     */
    public Mono<A2AResponse> submitSupportTicket(String baseUrl, Map<String, Object> ticketData) {
        return sendTask(baseUrl, "submit-support-ticket", ticketData);
    }

    /**
     * Gets the status of a task from an A2A agent server.
     */
    public Mono<A2AResponse> getTaskStatus(String baseUrl, String taskId) {
        String url = baseUrl + "/a2a";
        
        A2ARequest request = new A2ARequest(
                "2.0",
                "tasks/get",
                UUID.randomUUID().toString(),
                Map.of("id", taskId)
        );
        
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(A2AResponse.class)
                .timeout(TIMEOUT);
    }

    /**
     * Executes a task on a bookmarked agent by its ID.
     */
    public Mono<A2AResponse> executeOnAgent(Long agentId, String skillId, Map<String, Object> input) {
        return Mono.justOrEmpty(bookmarkService.getBookmarkById(agentId))
                .flatMap(bookmark -> {
                    bookmarkService.updateLastConnected(bookmark.getId(), null);
                    return sendTask(bookmark.getUrl(), skillId, input);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Agent not found: " + agentId)));
    }

    /**
     * Executes a task on all active agents matching a tag.
     */
    public Mono<Map<String, A2AResponse>> executeOnAgentsByTag(String tag, String skillId, Map<String, Object> input) {
        List<AgentBookmark> agents = bookmarkService.getBookmarksByTag(tag);
        
        if (agents.isEmpty()) {
            return Mono.just(Map.of());
        }
        
        Map<String, Mono<A2AResponse>> tasks = new HashMap<>();
        for (AgentBookmark agent : agents) {
            tasks.put(agent.getName(), sendTask(agent.getUrl(), skillId, input));
        }
        
        return Mono.zip(
                tasks.values(),
                results -> {
                    Map<String, A2AResponse> responseMap = new HashMap<>();
                    int i = 0;
                    for (String name : tasks.keySet()) {
                        responseMap.put(name, (A2AResponse) results[i++]);
                    }
                    return responseMap;
                }
        );
    }

    /**
     * Tests connectivity to an agent server.
     */
    public Mono<Boolean> testConnection(String baseUrl) {
        return fetchAgentCard(baseUrl)
                .map(card -> true)
                .onErrorReturn(false);
    }

    /**
     * Fetches and updates agent info for a bookmark.
     */
    public Mono<AgentCard> refreshAgentInfo(Long bookmarkId) {
        return Mono.justOrEmpty(bookmarkService.getBookmarkById(bookmarkId))
                .flatMap(bookmark -> fetchAgentCard(bookmark.getUrl())
                        .doOnSuccess(card -> {
                            bookmarkService.updateLastConnected(bookmark.getId(), card.version());
                        }))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bookmark not found")));
    }

    // A2A Protocol DTOs
    public record A2ARequest(
            String jsonrpc,
            String method,
            String id,
            Map<String, Object> params
    ) {}

    public record A2AResponse(
            String jsonrpc,
            String id,
            Object result,
            A2AError error
    ) {
        public boolean isSuccess() {
            return error == null;
        }
        
        @SuppressWarnings("unchecked")
        public Map<String, Object> getResultAsMap() {
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            }
            return Map.of();
        }
        
        public String getMessage() {
            Map<String, Object> resultMap = getResultAsMap();
            return (String) resultMap.getOrDefault("message", "");
        }
    }

    public record A2AError(
            int code,
            String message,
            Object data
    ) {}

    public record AgentCard(
            String name,
            String description,
            String url,
            String version,
            List<AgentSkill> skills,
            AgentCapabilities capabilities
    ) {}

    public record AgentSkill(
            String id,
            String name,
            String description,
            List<String> tags,
            Map<String, Object> inputSchema,
            Map<String, Object> outputSchema
    ) {}

    public record AgentCapabilities(
            boolean streaming,
            boolean pushNotifications,
            boolean stateTransitionHistory
    ) {}
}

