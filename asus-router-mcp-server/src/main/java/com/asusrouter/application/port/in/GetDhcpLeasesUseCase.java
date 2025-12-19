package com.asusrouter.application.port.in;

import com.asusrouter.domain.model.DhcpLease;
import com.asusrouter.mcp.annotations.McpTool;
import java.util.List;

/**
 * Port for retrieving DHCP lease table.
 */
@McpTool(
    name = "asus_router_get_dhcp_leases",
    description = "Retrieve DHCP lease information for clients that obtained IP addresses from the router's DHCP server",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface GetDhcpLeasesUseCase {
    /**
     * Execute the use case to get DHCP leases.
     * @return List of DHCP leases
     */
    List<DhcpLease> execute();
}
