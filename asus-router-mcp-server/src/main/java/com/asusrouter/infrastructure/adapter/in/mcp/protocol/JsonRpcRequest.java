package com.asusrouter.infrastructure.adapter.in.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON-RPC 2.0 request structure.
 * Represents incoming MCP tool calls.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcRequest {
    
    private String jsonrpc = "2.0";
    private String method;
    private Object params;
    private Object id;
    
    /**
     * Check if this is a notification (no response expected).
     */
    public boolean isNotification() {
        return id == null;
    }
}
