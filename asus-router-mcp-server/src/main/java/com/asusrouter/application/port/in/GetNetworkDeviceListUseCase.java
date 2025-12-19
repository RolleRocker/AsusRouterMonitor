package com.asusrouter.application.port.in;

import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for retrieving network device list.
 */
@McpTool(
    name = "asus_router_get_network_device_list",
    description = "Retrieve list of network devices with optional device name filter",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface GetNetworkDeviceListUseCase {
    /**
     * Execute the use case to get network device list.
     * @param deviceName Optional device name filter
     * @return Network device list as JSON string
     */
    String execute(
        @McpParameter(
            name = "device_name",
            description = "Optional device name to filter results (e.g., 'eth0', 'wl0')",
            required = false
        )
        String deviceName
    );
}
