package com.asusrouter.application.port.in;

import com.asusrouter.domain.exception.ErrorCode;
import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for checking if router is alive/responsive.
 */
@McpTool(
    name = "asus_router_is_alive",
    description = "Check if router is alive and responsive by verifying authentication and connectivity",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface IsAliveUseCase {
    /**
     * Execute the use case to check if router is alive.
     * @return true if router is responsive, false otherwise
     */
    boolean execute();
}
