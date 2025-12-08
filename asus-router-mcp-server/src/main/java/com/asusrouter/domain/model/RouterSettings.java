package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpSchema;

/**
 * Router settings and configuration from NVRAM.
 * Maps to Python RouterInfo.get_settings() return structure.
 */
@McpSchema(example = """
{
  "routerName": "RT-AX88U",
  "firmwareVersion": "3.0.0.4.388.21617",
  "lanIp": "192.168.1.1",
  "lanMask": "255.255.255.0",
  "lanGateway": "192.168.1.1",
  "lanDns": "192.168.1.1",
  "wanIp": "192.0.2.1",
  "wanMask": "255.255.255.0",
  "wanGateway": "192.0.2.254",
  "wanDns": "8.8.8.8",
  "wl0Ssid": "MyWiFi-2.4GHz",
  "wl1Ssid": "MyWiFi-5GHz",
  "wl0Closed": 0,
  "wl1Closed": 0,
  "wl0AuthMode": "psk2",
  "wl1AuthMode": "psk2",
  "wl0Crypto": "aes",
  "wl1Crypto": "aes",
  "wl0WpaKey": "********",
  "wl1WpaKey": "********",
  "dhcpEnable": 1,
  "dhcpStart": "192.168.1.100",
  "dhcpEnd": "192.168.1.200"
}
""")
public record RouterSettings(
    String routerName,
    String firmwareVersion,
    
    @McpParameter(description = "LAN IP address", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    IpAddress lanIp,
    
    @McpParameter(description = "LAN subnet mask", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    Netmask lanMask,
    
    @McpParameter(description = "LAN gateway", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    IpAddress lanGateway,
    
    String lanDns,
    
    @McpParameter(description = "WAN IP address", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    IpAddress wanIp,
    
    @McpParameter(description = "WAN subnet mask", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    Netmask wanMask,
    
    @McpParameter(description = "WAN gateway", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    IpAddress wanGateway,
    
    String wanDns,
    String wl0Ssid,
    String wl1Ssid,
    int wl0Closed,
    int wl1Closed,
    String wl0AuthMode,
    String wl1AuthMode,
    String wl0Crypto,
    String wl1Crypto,
    String wl0WpaKey,
    String wl1WpaKey,
    int dhcpEnable,
    
    @McpParameter(description = "DHCP range start", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    IpAddress dhcpStart,
    
    @McpParameter(description = "DHCP range end", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    IpAddress dhcpEnd
) {
    public RouterSettings {
        if (routerName == null || lanIp == null) {
            throw new IllegalArgumentException("Required router settings (routerName, lanIp) cannot be null");
        }
    }
    
    /**
     * Check if DHCP server is enabled.
     */
    public boolean isDhcpEnabled() {
        return dhcpEnable == 1;
    }
    
    /**
     * Check if 2.4GHz SSID is hidden.
     */
    public boolean is24GHzHidden() {
        return wl0Closed == 1;
    }
    
    /**
     * Check if 5GHz SSID is hidden.
     */
    public boolean is5GHzHidden() {
        return wl1Closed == 1;
    }
}
