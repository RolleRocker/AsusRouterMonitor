package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpSchema;

/**
 * Combines total traffic and current speed.
 */
@McpSchema(example = """
{
  "total": {
    "sent": 15901.92873764038,
    "recv": 10926.945571899414
  },
  "speed": {
    "sent": 10.24,
    "recv": 25.6
  }
}
""")
public record TrafficWithSpeed(
    TrafficTotal total,
    TrafficSpeed speed
) {
    public TrafficWithSpeed {
        if (total == null || speed == null) {
            throw new IllegalArgumentException("Traffic components cannot be null");
        }
    }
}
