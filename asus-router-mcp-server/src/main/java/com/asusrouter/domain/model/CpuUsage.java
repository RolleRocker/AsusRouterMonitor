package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpSchema;

/**
 * Represents router CPU usage statistics.
 */
@McpSchema(example = """
{
  "cpu1Total": "38106047",
  "cpu1Usage": "3395512",
  "cpu2Total": "38106008",
  "cpu2Usage": "2384694"
}
""")
public record CpuUsage(
    String cpu1Total,
    String cpu1Usage,
    String cpu2Total,
    String cpu2Usage
) {
    public CpuUsage {
        if (cpu1Total == null || cpu1Usage == null || cpu2Total == null || cpu2Usage == null) {
            throw new IllegalArgumentException("CPU fields cannot be null");
        }
    }
    
    /**
     * Calculate CPU1 usage percentage.
     */
    public double getCpu1Percentage() {
        long total = Long.parseLong(cpu1Total);
        long usage = Long.parseLong(cpu1Usage);
        return total > 0 ? (usage * 100.0 / total) : 0.0;
    }
    
    /**
     * Calculate CPU2 usage percentage.
     */
    public double getCpu2Percentage() {
        long total = Long.parseLong(cpu2Total);
        long usage = Long.parseLong(cpu2Usage);
        return total > 0 ? (usage * 100.0 / total) : 0.0;
    }
    
    /**
     * Calculate average CPU usage percentage.
     */
    public double getAveragePercentage() {
        return (getCpu1Percentage() + getCpu2Percentage()) / 2.0;
    }
}
