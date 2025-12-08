package com.asusrouter.application.port.in;

import com.asusrouter.domain.exception.ErrorCode;
import com.asusrouter.domain.model.TrafficTotal;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for retrieving total network traffic since last boot.
 */
@McpTool(
    name = "asus_router_get_traffic_total",
    description = "Retrieve total network traffic in Megabits since last router reboot (sent and received)",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface GetTrafficTotalUseCase {
    /**
     * Execute the use case to get total traffic.
     * @return Total traffic information
     */
    TrafficTotal execute();
}
