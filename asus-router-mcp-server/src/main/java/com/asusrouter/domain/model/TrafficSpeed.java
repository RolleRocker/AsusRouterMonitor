package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpSchema;

/**
 * Represents current network traffic speed (in Kilobits per second).
 */
@McpSchema(example = """
{
  "sent": 10.24,
  "recv": 25.6
}
""")
public record TrafficSpeed(
    double sent,
    double recv
) {
    public TrafficSpeed {
        if (sent < 0 || recv < 0) {
            throw new IllegalArgumentException("Traffic speed values cannot be negative");
        }
    }
}
