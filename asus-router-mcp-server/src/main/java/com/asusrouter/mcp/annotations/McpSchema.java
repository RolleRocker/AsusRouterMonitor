package com.asusrouter.mcp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class/record as an MCP schema with an example.
 * Used for compile-time generation of JSON schemas for MCP tool outputs.
 * 
 * <p>Example usage:
 * <pre>
 * {@literal @}McpSchema(example = """
 * {
 *   "since": "Thu, 22 Jul 2021 14:32:38 +0200",
 *   "uptime": "375001"
 * }
 * """)
 * public record Uptime(
 *     String since,
 *     String uptime
 * ) {}
 * </pre>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface McpSchema {
    /**
     * A JSON example of this schema.
     * This example will be used to generate the JSON schema
     * and provide documentation for the MCP tool.
     * 
     * <p>Use text blocks (""") for multi-line JSON examples.
     */
    String example();
}
