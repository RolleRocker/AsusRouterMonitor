package com.asusrouter.application.port.in;


import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for executing custom NVRAM commands.
 */
@McpTool(
    name = "asus_router_get_nvram",
    description = "Execute custom NVRAM command to retrieve specific router configuration values from NVRAM storage",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR", "INVALID_COMMAND"}
)
public interface GetNvramUseCase {
    /**
     * Execute the use case to get NVRAM value.
     * @param nvramCommand NVRAM command to execute
     * @return NVRAM command result as string
     */
    String execute(
        @McpParameter(
            name = "nvram_command",
            description = "NVRAM command to execute (e.g., 'nvram get wan0_ipaddr')",
            required = true
        )
        String nvramCommand
    );
}
