package com.asusrouter.domain.exception;

/**
 * Error codes for router-related exceptions.
 * These codes are used in MCP tool error responses.
 */
public enum ErrorCode {
    ROUTER_AUTH_FAILED("Failed to authenticate with router", -32001),
    ROUTER_COMM_ERROR("Communication error with router", -32002),
    ROUTER_PARSE_ERROR("Failed to parse router response", -32003),
    CLIENT_NOT_FOUND("Client with specified MAC address not found", -32004),
    INVALID_RESPONSE("Invalid response from router", -32005),
    NETWORK_TIMEOUT("Network timeout while communicating with router", -32006),
    INVALID_COMMAND("Invalid command or operation", -32007),
    INVALID_PARAMETER("Invalid parameter value", -32008);
    
    private final String defaultMessage;
    private final int code;
    
    ErrorCode(String defaultMessage, int code) {
        this.defaultMessage = defaultMessage;
        this.code = code;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    public int getCode() {
        return code;
    }
}
