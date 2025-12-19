package com.asusrouter.application.port.in;

import com.asusrouter.domain.model.CpuUsage;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for retrieving router CPU usage statistics.
 */
@McpTool(
    name = "asus_router_get_cpu_usage",
    description = "Retrieve router CPU usage statistics for all CPU cores including total and usage counters",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface GetCpuUsageUseCase {
    /**
     * Execute the use case to get CPU usage.
     * @return CPU usage information
     */
    CpuUsage execute();
}
