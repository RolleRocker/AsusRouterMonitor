package com.asusrouter.domain.model;

import com.asusrouter.mcp.annotations.McpSchema;

/**
 * Represents router memory usage statistics.
 */
@McpSchema(example = """
{
  "memTotal": "262144",
  "memFree": "107320",
  "memUsed": "154824"
}
""")
public record MemoryUsage(
    String memTotal,
    String memFree,
    String memUsed
) {
    public MemoryUsage {
        if (memTotal == null || memFree == null || memUsed == null) {
            throw new IllegalArgumentException("Memory fields cannot be null");
        }
    }
    
    /**
     * Get total memory in KB.
     */
    public long getTotalKB() {
        return Long.parseLong(memTotal);
    }
    
    /**
     * Get free memory in KB.
     */
    public long getFreeKB() {
        return Long.parseLong(memFree);
    }
    
    /**
     * Get used memory in KB.
     */
    public long getUsedKB() {
        return Long.parseLong(memUsed);
    }
    
    /**
     * Calculate memory usage percentage.
     */
    public double getUsagePercentage() {
        long total = getTotalKB();
        long used = getUsedKB();
        return total > 0 ? (used * 100.0 / total) : 0.0;
    }
}
