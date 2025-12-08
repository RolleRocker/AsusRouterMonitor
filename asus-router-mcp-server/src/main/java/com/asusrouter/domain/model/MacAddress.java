package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpParameter;

/**
 * Value object representing a MAC address.
 * Validates the format on construction.
 * Accepts both colon and hyphen separators.
 */
@McpParameter(
    pattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
    description = "Valid MAC address in format XX:XX:XX:XX:XX:XX or XX-XX-XX-XX-XX-XX"
)
public record MacAddress(String value) {
    private static final String MAC_PATTERN = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
    
    public MacAddress {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("MAC address cannot be null or empty");
        }
        if (!value.matches(MAC_PATTERN)) {
            throw new IllegalArgumentException("Invalid MAC address format: " + value);
        }
    }
    
    /**
     * Normalize MAC address to use colon separator and uppercase.
     */
    public String normalized() {
        return value.replace('-', ':').toUpperCase();
    }
    
    @Override
    public String toString() {
        return value;
    }
}
