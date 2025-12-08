package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpSchema;

/**
 * Represents a DHCP lease entry.
 * Maps to Python RouterInfo.get_dhcp_leases() list entries.
 */
@McpSchema(example = """
{
  "hostname": "MyPhone",
  "mac": "AA:BB:CC:DD:EE:FF",
  "ip": "192.168.1.100",
  "expires": "3600"
}
""")
public record DhcpLease(
    String hostname,
    
    @McpParameter(description = "Client MAC address", pattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
    MacAddress mac,
    
    @McpParameter(description = "Assigned IP address", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    IpAddress ip,
    
    String expires
) {
    public DhcpLease {
        if (mac == null || ip == null) {
            throw new IllegalArgumentException("Required DHCP lease fields (mac, ip) cannot be null");
        }
    }
    
    /**
     * Get lease expiration time in seconds.
     */
    public long getExpiresSeconds() {
        try {
            return Long.parseLong(expires);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
