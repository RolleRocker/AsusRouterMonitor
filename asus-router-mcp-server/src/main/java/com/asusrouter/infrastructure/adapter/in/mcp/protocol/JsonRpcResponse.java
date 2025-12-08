package com.asusrouter.infrastructure.adapter.in.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON-RPC 2.0 response structure.
 * Represents outgoing MCP tool results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {
    
    private String jsonrpc = "2.0";
    private Object result;
    private JsonRpcError error;
    private Object id;
    
    /**
     * Create success response.
     */
    public static JsonRpcResponse success(Object result, Object id) {
        return new JsonRpcResponse("2.0", result, null, id);
    }
    
    /**
     * Create error response.
     */
    public static JsonRpcResponse error(JsonRpcError error, Object id) {
        return new JsonRpcResponse("2.0", null, error, id);
    }
}
