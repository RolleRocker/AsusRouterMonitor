package com.asusrouter.application.port.in;

import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for retrieving WAN link information.
 */
@McpTool(
    name = "asus_router_get_wan_link",
    description = "Retrieve WAN link information with optional unit specification for WAN interface",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface GetWanLinkUseCase {
    /**
     * Execute the use case to get WAN link information.
     * @param unit WAN unit number (default: 0)
     * @return WAN link information as JSON string
     */
    String execute(
        @McpParameter(
            name = "unit",
            description = "WAN unit number (0 for primary WAN, 1 for secondary WAN in dual-WAN setups)",
            required = false
        )
        Integer unit
    );
}
