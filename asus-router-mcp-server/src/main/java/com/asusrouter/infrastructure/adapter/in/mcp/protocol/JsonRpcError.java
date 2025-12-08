package com.asusrouter.infrastructure.adapter.in.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON-RPC 2.0 error structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcError {
    
    private int code;
    private String message;
    private Object data;
    
    // Standard JSON-RPC error codes
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
    
    /**
     * Create error without additional data.
     */
    public JsonRpcError(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public static JsonRpcError parseError(String detail) {
        return new JsonRpcError(PARSE_ERROR, "Parse error", detail);
    }
    
    public static JsonRpcError invalidRequest(String detail) {
        return new JsonRpcError(INVALID_REQUEST, "Invalid Request", detail);
    }
    
    public static JsonRpcError methodNotFound(String method) {
        return new JsonRpcError(METHOD_NOT_FOUND, "Method not found", "Method '" + method + "' does not exist");
    }
    
    public static JsonRpcError invalidParams(String detail) {
        return new JsonRpcError(INVALID_PARAMS, "Invalid params", detail);
    }
    
    public static JsonRpcError internalError(String detail) {
        return new JsonRpcError(INTERNAL_ERROR, "Internal error", detail);
    }
}
