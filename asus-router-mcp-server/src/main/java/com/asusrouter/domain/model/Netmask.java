package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpParameter;

/**
 * Value object representing a network netmask.
 * Validates the format on construction.
 */
@McpParameter(
    pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$",
    description = "Valid netmask in format XXX.XXX.XXX.XXX"
)
public record Netmask(String value) {
    private static final String NETMASK_PATTERN = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
    
    public Netmask {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Netmask cannot be null or empty");
        }
        if (!value.matches(NETMASK_PATTERN)) {
            throw new IllegalArgumentException("Invalid netmask format: " + value);
        }
        // Validate each octet is 0-255
        String[] octets = value.split("\\.");
        for (String octet : octets) {
            int num = Integer.parseInt(octet);
            if (num < 0 || num > 255) {
                throw new IllegalArgumentException("Invalid netmask octet: " + octet);
            }
        }
    }
    
    @Override
    public String toString() {
        return value;
    }
}
