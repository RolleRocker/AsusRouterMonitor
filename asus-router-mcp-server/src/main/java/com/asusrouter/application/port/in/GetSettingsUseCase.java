package com.asusrouter.application.port.in;

import com.asusrouter.domain.model.RouterSettings;
import com.asusrouter.mcp.annotations.McpTool;

/**
 * Port for retrieving router settings and configuration.
 */
@McpTool(
    name = "asus_router_get_settings",
    description = "Retrieve router settings and configuration from NVRAM including network settings, WiFi configuration, and DHCP settings",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface GetSettingsUseCase {
    /**
     * Execute the use case to get router settings.
     * @return Router settings
     */
    RouterSettings execute();
}
