package hr.example.a2a.model;

import java.util.Map;

/**
 * Request model for A2A protocol JSON-RPC style requests.
 */
public record A2ARequest(
        String jsonrpc,
        String method,
        String id,
        Map<String, Object> params
) {
    public static final String JSON_RPC_VERSION = "2.0";
}

