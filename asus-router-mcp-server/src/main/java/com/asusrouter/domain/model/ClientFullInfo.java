package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpParameter;
import com.asusrouter.mcp.annotations.McpSchema;

/**
 * Complete information about a connected client.
 * Maps to Python RouterInfo.get_client_full_info() return structure.
 */
@McpSchema(example = """
{
  "name": "MyPhone",
  "nickName": "My Phone",
  "ip": "192.168.1.100",
  "mac": "AA:BB:CC:DD:EE:FF",
  "from": "networkmapd",
  "macRepeat": 1,
  "isGateway": false,
  "isWebStorage": false,
  "isPrinter": false,
  "isITunes": false,
  "dpiType": "",
  "dpiDevice": "",
  "vendor": "Apple",
  "osType": "iOS",
  "ssid": "MyWiFi",
  "isWL": 1,
  "isOnline": true,
  "rssi": -45,
  "curTx": "866 Mbps",
  "curRx": "866 Mbps",
  "totalTx": "1234567890",
  "totalRx": "9876543210",
  "wlConnectTime": 3600,
  "ipMethod": "dhcp",
  "opMode": 1,
  "ROG": false,
  "group": "",
  "callback": "",
  "keeparp": "",
  "qosLevel": "",
  "wtfast": false,
  "internetMode": "allow",
  "internetState": 1
}
""")
public record ClientFullInfo(
    String name,
    String nickName,
    
    @McpParameter(description = "Client IP address", pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    IpAddress ip,
    
    @McpParameter(description = "Client MAC address", pattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
    MacAddress mac,
    
    String from,
    int macRepeat,
    boolean isGateway,
    boolean isWebStorage,
    boolean isPrinter,
    boolean isITunes,
    String dpiType,
    String dpiDevice,
    String vendor,
    String osType,
    String ssid,
    int isWL,
    boolean isOnline,
    Integer rssi,
    String curTx,
    String curRx,
    String totalTx,
    String totalRx,
    Integer wlConnectTime,
    String ipMethod,
    Integer opMode,
    boolean ROG,
    String group,
    String callback,
    String keeparp,
    String qosLevel,
    boolean wtfast,
    String internetMode,
    Integer internetState
) {
    public ClientFullInfo {
        if (name == null || ip == null || mac == null) {
            throw new IllegalArgumentException("Required client fields (name, ip, mac) cannot be null");
        }
    }
    
    /**
     * Check if client is connected via wireless.
     */
    public boolean isWireless() {
        return isWL == 1;
    }
    
    /**
     * Check if internet access is allowed.
     */
    public boolean hasInternetAccess() {
        return "allow".equalsIgnoreCase(internetMode) && (internetState == null || internetState == 1);
    }
}
