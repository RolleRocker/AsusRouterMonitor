package com.asusrouter.application.port.in;

import com.asusrouter.domain.model.MemoryUsage;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for retrieving router memory usage statistics.
 */
@McpTool(
    name = "asus_router_get_memory_usage",
    description = "Return current memory usage information including total, used, and free memory",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface GetMemoryUsageUseCase {
    /**
     * Execute the use case to get memory usage.
     * @return Memory usage information
     */
    MemoryUsage execute();
}
