package com.asusrouter.mcp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark an interface as an MCP tool.
 * Used for compile-time generation of MCP tool schemas.
 * 
 * <p>Example usage:
 * <pre>
 * {@literal @}McpTool(
 *     name = "asus_router_get_uptime",
 *     description = "Return uptime of the router with last boot time and uptime in seconds",
 *     errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
 * )
 * public interface GetUptimePort {
 *     Uptime execute();
 * }
 * </pre>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface McpTool {
    /**
     * The name of the MCP tool (e.g., "asus_router_get_uptime").
     * Must be unique across all tools in the system.
     */
    String name();
    
    /**
     * A detailed description of what the tool does.
     * This description will be included in the generated MCP schema
     * to help AI assistants understand the tool's purpose.
     */
    String description();
    
    /**
     * Array of error codes that this tool can return.
     * Each error code should correspond to a domain exception.
     * 
     * <p>Example: {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR", "CLIENT_NOT_FOUND"}
     */
    String[] errorCodes() default {};
}
