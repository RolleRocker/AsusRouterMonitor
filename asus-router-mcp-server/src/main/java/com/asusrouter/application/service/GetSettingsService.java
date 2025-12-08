package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetSettingsUseCase;
import com.asusrouter.application.port.out.RouterNvramPort;
import com.asusrouter.domain.model.IpAddress;
import com.asusrouter.domain.model.Netmask;
import com.asusrouter.domain.model.RouterSettings;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case implementation for retrieving router settings from NVRAM.
 */
@Service
@RequiredArgsConstructor
public class GetSettingsService implements GetSettingsUseCase {
    
    private final RouterNvramPort routerNvramPort;
    private final ObjectMapper objectMapper;
    
    @Override
    public RouterSettings execute() {
        String rawResponse = routerNvramPort.getSettings();
        return parseRouterSettings(rawResponse);
    }
    
    /**
     * Parse router settings from NVRAM dump.
     * Expected format: JSON object with nvram key-value pairs.
     */
    private RouterSettings parseRouterSettings(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            
            return new RouterSettings(
                getStringValue(root, "productid", "RT-UNKNOWN"),
                getStringValue(root, "firmver", "unknown"),
                new IpAddress(getStringValue(root, "lan_ipaddr", "192.168.1.1")),
                new Netmask(getStringValue(root, "lan_netmask", "255.255.255.0")),
                new IpAddress(getStringValue(root, "lan_gateway", "192.168.1.1")),
                getStringValue(root, "lan_dns", "192.168.1.1"),
                new IpAddress(getStringValue(root, "wan0_ipaddr", "0.0.0.0")),
                new Netmask(getStringValue(root, "wan0_netmask", "0.0.0.0")),
                new IpAddress(getStringValue(root, "wan0_gateway", "0.0.0.0")),
                getStringValue(root, "wan0_dns", ""),
                getStringValue(root, "wl0_ssid", ""),
                getStringValue(root, "wl1_ssid", ""),
                getIntValue(root, "wl0_closed", 0),
                getIntValue(root, "wl1_closed", 0),
                getStringValue(root, "wl0_auth_mode_x", "open"),
                getStringValue(root, "wl1_auth_mode_x", "open"),
                getStringValue(root, "wl0_crypto", "none"),
                getStringValue(root, "wl1_crypto", "none"),
                getStringValue(root, "wl0_wpa_psk", "********"),
                getStringValue(root, "wl1_wpa_psk", "********"),
                getIntValue(root, "dhcp_enable_x", 1),
                new IpAddress(getStringValue(root, "dhcp_start", "192.168.1.2")),
                new IpAddress(getStringValue(root, "dhcp_end", "192.168.1.254"))
            );
            
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse router settings response", e);
        }
    }
    
    private String getStringValue(JsonNode root, String key, String defaultValue) {
        JsonNode node = root.path(key);
        return node.isMissingNode() ? defaultValue : node.asText(defaultValue);
    }
    
    private int getIntValue(JsonNode root, String key, int defaultValue) {
        JsonNode node = root.path(key);
        return node.isMissingNode() ? defaultValue : node.asInt(defaultValue);
    }
}
