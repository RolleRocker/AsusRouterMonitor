package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpSchema;

/**
 * Represents WAN connection status.
 */
@McpSchema(example = """
{
  "status": "connected",
  "statusCode": 1,
  "ip": "192.0.2.1",
  "gateway": "192.0.2.254",
  "mask": "255.255.255.0",
  "dns": ["8.8.8.8", "8.8.4.4"]
}
""")
public record WanStatus(
    String status,
    int statusCode,
    
    @McpParameter(description = "WAN IP address", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    IpAddress ip,
    
    @McpParameter(description = "Gateway IP address", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    IpAddress gateway,
    
    @McpParameter(description = "Network mask", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    Netmask mask,
    
    @McpParameter(description = "DNS server addresses")
    java.util.List<IpAddress> dns
) {
    public WanStatus {
        if (status == null || ip == null || gateway == null || mask == null || dns == null) {
            throw new IllegalArgumentException("WAN status fields cannot be null");
        }
        dns = java.util.List.copyOf(dns); // Make immutable
    }
    
    /**
     * Check if connection is established.
     */
    public boolean isConnected() {
        return statusCode == 1 || "connected".equalsIgnoreCase(status);
    }
}
