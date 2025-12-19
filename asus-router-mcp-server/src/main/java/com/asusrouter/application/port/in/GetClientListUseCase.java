package com.asusrouter.application.port.in;

import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for retrieving client list in different formats.
 */
@McpTool(
    name = "asus_router_get_client_list",
    description = "Retrieve list of known clients in specified format (0=basic, 1=with details, 2=full JSON)",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR", "INVALID_PARAMETER"}
)
public interface GetClientListUseCase {
    /**
     * Execute the use case to get client list.
     * @param format Output format (0, 1, or 2)
     * @return Client list as JSON string
     */
    String execute(
        @McpParameter(
            name = "format",
            description = "Output format: 0=basic list, 1=with details, 2=full JSON structure",
            required = false
        )
        Integer format
    );
}
