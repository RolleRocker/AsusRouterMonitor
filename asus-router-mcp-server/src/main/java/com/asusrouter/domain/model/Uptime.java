package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpSchema;

/**
 * Represents router uptime information.
 */
@McpSchema(example = """
{
  "since": "Thu, 22 Jul 2021 14:32:38 +0200",
  "uptime": "375001"
}
""")
public record Uptime(
    String since,
    String uptime
) {
    public Uptime {
        if (since == null || uptime == null) {
            throw new IllegalArgumentException("Uptime fields cannot be null");
        }
    }
    
    /**
     * Get uptime as seconds.
     */
    public long getUptimeSeconds() {
        return Long.parseLong(uptime);
    }
}
