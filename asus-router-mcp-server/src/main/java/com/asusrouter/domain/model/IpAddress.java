package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpParameter;

/**
 * Value object representing an IPv4 address.
 * Validates the format on construction.
 */
@McpParameter(
    pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$",
    description = "Valid IPv4 address in format XXX.XXX.XXX.XXX"
)
public record IpAddress(String value) {
    private static final String IP_PATTERN = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
    
    public IpAddress {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("IP address cannot be null or empty");
        }
        if (!value.matches(IP_PATTERN)) {
            throw new IllegalArgumentException("Invalid IP address format: " + value);
        }
        // Validate each octet is 0-255
        String[] octets = value.split("\\.");
        for (String octet : octets) {
            int num = Integer.parseInt(octet);
            if (num < 0 || num > 255) {
                throw new IllegalArgumentException("Invalid IP address octet: " + octet);
            }
        }
    }
    
    @Override
    public String toString() {
        return value;
    }
}
