package com.asusrouter.application.port.in;

import com.asusrouter.domain.model.ClientFullInfo;
import com.asusrouter.domain.model.MacAddress;
import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for retrieving complete client information by MAC address.
 */
@McpTool(
    name = "asus_router_get_client_full_info",
    description = "Retrieve complete information about a specific connected client including connection details, traffic statistics, and configuration",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR", "CLIENT_NOT_FOUND"}
)
public interface GetClientFullInfoUseCase {
    /**
     * Execute the use case to get full client information.
     * @param mac Client MAC address
     * @return Complete client information
     */
    ClientFullInfo execute(
        @McpParameter(
            name = "mac",
            description = "Client MAC address (format: AA:BB:CC:DD:EE:FF or AA-BB-CC-DD-EE-FF)",
            required = true,
            pattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
        )
        MacAddress mac
    );
}
