package com.asusrouter.infrastructure.adapters.in.mcp.generated;

/**
 * Interface for all generated MCP tool schemas.
 * Each tool schema provides metadata about an MCP tool including
 * its name, description, input/output schemas, and error definitions.
 */
public interface ToolSchema {
    /**
     * @return The unique name of the MCP tool
     */
    String getName();
    
    /**
     * @return A detailed description of what the tool does
     */
    String getDescription();
    
    /**
     * @return JSON schema for the tool's input parameters
     */
    default String getInputSchema() {
        return "{}";
    }
    
    /**
     * @return JSON schema for the tool's output
     */
    default String getOutputSchema() {
        return "{}";
    }
    
    /**
     * @return List of possible error codes and messages
     */
    default java.util.List<ErrorDefinition> getErrors() {
        return java.util.Collections.emptyList();
    }
    
    /**
     * Represents an error that can be returned by the tool.
     */
    record ErrorDefinition(String code, String message) {}
}
