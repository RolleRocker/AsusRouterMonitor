package com.asusrouter.application.port.in;


import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for displaying formatted router information.
 * Equivalent to Python's ShowRouterInfo functionality.
 */
@McpTool(
    name = "asus_router_show_info",
    description = "Display comprehensive router information in a formatted manner including system stats, network status, and connected clients",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface ShowRouterInfoUseCase {
    /**
     * Execute the use case to display router information.
     * @param detailed Whether to show detailed information
     * @return Formatted router information as string
     */
    String execute(
        @McpParameter(
            name = "detailed",
            description = "Show detailed information including all clients and extended statistics",
            required = false
        )
        Boolean detailed
    );
}
