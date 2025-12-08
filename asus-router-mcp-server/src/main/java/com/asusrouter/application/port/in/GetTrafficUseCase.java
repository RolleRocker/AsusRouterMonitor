package com.asusrouter.application.port.in;

import com.asusrouter.domain.exception.ErrorCode;
import com.asusrouter.domain.model.TrafficWithSpeed;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for retrieving network traffic with current speed.
 */
@McpTool(
    name = "asus_router_get_traffic",
    description = "Retrieve network traffic statistics including total traffic since boot and current transfer speeds",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface GetTrafficUseCase {
    /**
     * Execute the use case to get traffic with speed.
     * @return Traffic with speed information
     */
    TrafficWithSpeed execute();
}
