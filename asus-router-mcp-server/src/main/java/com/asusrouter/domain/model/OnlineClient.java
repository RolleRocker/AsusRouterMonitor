package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpSchema;

/**
 * Represents an online client with basic information.
 * Maps to Python RouterInfo.get_online_clients() list entries.
 */
@McpSchema(example = """
{
  "mac": "AA:BB:CC:DD:EE:FF",
  "ip": "192.168.1.100"
}
""")
public record OnlineClient(
    @McpParameter(description = "Client MAC address", pattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
    MacAddress mac,
    
    @McpParameter(description = "Client IP address", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    IpAddress ip
) {
    public OnlineClient {
        if (mac == null || ip == null) {
            throw new IllegalArgumentException("Online client fields (mac, ip) cannot be null");
        }
    }
}
