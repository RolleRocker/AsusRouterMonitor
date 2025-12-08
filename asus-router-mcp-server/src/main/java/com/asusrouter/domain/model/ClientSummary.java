package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpSchema;

/**
 * Summary information about a connected client.
 * Maps to Python RouterInfo.get_client_info_summary() return structure.
 */
@McpSchema(example = """
{
  "nickName": "My Phone",
  "ip": "192.168.1.100",
  "mac": "AA:BB:CC:DD:EE:FF",
  "isOnline": true,
  "name": "MyPhone",
  "vendor": "Apple",
  "isWL": 1,
  "rssi": -45,
  "curTx": "866 Mbps"
}
""")
public record ClientSummary(
    String nickName,
    
    @McpParameter(description = "Client IP address", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    IpAddress ip,
    
    @McpParameter(description = "Client MAC address", pattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
    MacAddress mac,
    
    boolean isOnline,
    String name,
    String vendor,
    int isWL,
    Integer rssi,
    String curTx
) {
    public ClientSummary {
        if (ip == null || mac == null) {
            throw new IllegalArgumentException("Required client fields (ip, mac) cannot be null");
        }
    }
    
    /**
     * Check if client is connected via wireless.
     */
    public boolean isWireless() {
        return isWL == 1;
    }
}
