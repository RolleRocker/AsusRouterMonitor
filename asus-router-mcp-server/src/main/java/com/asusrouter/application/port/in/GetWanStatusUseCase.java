package com.asusrouter.application.port.in;

import com.asusrouter.domain.model.WanStatus;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for retrieving WAN connection status.
 */
@McpTool(
    name = "asus_router_get_wan_status",
    description = "Retrieve WAN connection status including IP address, gateway, netmask, DNS servers, and connection state",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface GetWanStatusUseCase {
    /**
     * Execute the use case to get WAN status.
     * @return WAN status information
     */
    WanStatus execute();
}
