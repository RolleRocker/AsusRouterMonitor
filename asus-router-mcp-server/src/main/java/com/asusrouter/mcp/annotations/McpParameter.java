package com.asusrouter.mcp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field/parameter/type with validation constraints for MCP.
 * Used for compile-time generation of JSON schema validation rules.
 * 
 * <p>Example usage:
 * <pre>
 * {@literal @}McpParameter(
 *     required = true,
 *     pattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
 *     description = "Valid MAC address in format XX:XX:XX:XX:XX:XX"
 * )
 * public record MacAddress(String value) {
 *     public MacAddress {
 *         if (!value.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")) {
 *             throw new IllegalArgumentException("Invalid MAC address format");
 *         }
 *     }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
public @interface McpParameter {
    /**
     * Name of the parameter for MCP tool invocation.
     * This will be used as the property name in JSON-RPC params.
     */
    String name() default "";
    
    /**
     * Whether this parameter is required.
     * Default is true.
     */
    boolean required() default true;
    
    /**
     * Regular expression pattern for string validation.
     * Empty string means no pattern validation.
     */
    String pattern() default "";
    
    /**
     * Minimum value for numeric parameters.
     * Default is Double.MIN_VALUE (no minimum).
     */
    double min() default Double.MIN_VALUE;
    
    /**
     * Maximum value for numeric parameters.
     * Default is Double.MAX_VALUE (no maximum).
     */
    double max() default Double.MAX_VALUE;
    
    /**
     * Description of the parameter for documentation.
     * This will be included in the generated JSON schema.
     */
    String description();
}
