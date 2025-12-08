package com.asusrouter.application.port.in;

import com.asusrouter.domain.exception.ErrorCode;
import com.asusrouter.domain.model.Uptime;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for retrieving router uptime information.
 */
@McpTool(
    name = "asus_router_get_uptime",
    description = "Retrieve router uptime information including boot time and seconds since last reboot",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface GetUptimeUseCase {
    /**
     * Execute the use case to get router uptime.
     * @return Uptime information
     */
    Uptime execute();
}
