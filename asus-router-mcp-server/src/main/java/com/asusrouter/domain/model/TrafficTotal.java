package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpSchema;

/**
 * Represents total network traffic since last boot (in Megabits).
 */
@McpSchema(example = """
{
  "sent": 15901.92873764038,
  "recv": 10926.945571899414
}
""")
public record TrafficTotal(
    double sent,
    double recv
) {
    public TrafficTotal {
        if (sent < 0 || recv < 0) {
            throw new IllegalArgumentException("Traffic values cannot be negative");
        }
    }
    
    /**
     * Get total traffic (sent + received).
     */
    public double getTotal() {
        return sent + recv;
    }
}
