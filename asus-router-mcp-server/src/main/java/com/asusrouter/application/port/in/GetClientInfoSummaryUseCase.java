package com.asusrouter.application.port.in;

import com.asusrouter.domain.exception.ErrorCode;
import com.asusrouter.domain.model.ClientSummary;
import com.asusrouter.domain.model.MacAddress;
import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for retrieving client summary information by MAC address.
 */
@McpTool(
    name = "asus_router_get_client_info_summary",
    description = "Retrieve summary information about a specific connected client including name, IP, connection type, and signal strength",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR", "CLIENT_NOT_FOUND"}
)
public interface GetClientInfoSummaryUseCase {
    /**
     * Execute the use case to get client summary.
     * @param mac Client MAC address
     * @return Client summary information
     */
    ClientSummary execute(
        @McpParameter(
            name = "mac",
            description = "Client MAC address (format: AA:BB:CC:DD:EE:FF or AA-BB-CC-DD-EE-FF)",
            required = true,
            pattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
        )
        MacAddress mac
    );
}
