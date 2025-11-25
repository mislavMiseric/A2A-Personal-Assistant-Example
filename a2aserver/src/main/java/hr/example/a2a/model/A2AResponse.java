package hr.example.a2a.model;

import java.util.Map;

/**
 * Response model for A2A protocol JSON-RPC style responses.
 */
public record A2AResponse(
        String jsonrpc,
        String id,
        Object result,
        A2AError error
) {
    public static A2AResponse success(String id, Object result) {
        return new A2AResponse("2.0", id, result, null);
    }

    public static A2AResponse error(String id, int code, String message) {
        return new A2AResponse("2.0", id, null, new A2AError(code, message, null));
    }

    public static A2AResponse error(String id, int code, String message, Object data) {
        return new A2AResponse("2.0", id, null, new A2AError(code, message, data));
    }

    public record A2AError(
            int code,
            String message,
            Object data
    ) {}

    // Standard JSON-RPC error codes
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;

    // A2A specific error codes
    public static final int TASK_NOT_FOUND = -32001;
    public static final int TASK_FAILED = -32002;
}

