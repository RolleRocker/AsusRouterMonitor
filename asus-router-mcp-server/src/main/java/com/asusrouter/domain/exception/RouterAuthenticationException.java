package com.asusrouter.domain.exception;

/**
 * Exception thrown when authentication with the router fails.
 */
public class RouterAuthenticationException extends RouterException {
    
    public RouterAuthenticationException() {
        super(ErrorCode.ROUTER_AUTH_FAILED);
    }
    
    public RouterAuthenticationException(String message) {
        super(ErrorCode.ROUTER_AUTH_FAILED, message);
    }
    
    public RouterAuthenticationException(String message, Throwable cause) {
        super(ErrorCode.ROUTER_AUTH_FAILED, message, cause);
    }
    
    public RouterAuthenticationException(Throwable cause) {
        super(ErrorCode.ROUTER_AUTH_FAILED, cause);
    }
}
