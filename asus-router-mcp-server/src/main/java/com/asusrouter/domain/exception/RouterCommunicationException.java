package com.asusrouter.domain.exception;

/**
 * Exception thrown when communication with the router fails.
 */
public class RouterCommunicationException extends RouterException {
    
    public RouterCommunicationException() {
        super(ErrorCode.ROUTER_COMM_ERROR);
    }
    
    public RouterCommunicationException(String message) {
        super(ErrorCode.ROUTER_COMM_ERROR, message);
    }
    
    public RouterCommunicationException(String message, Throwable cause) {
        super(ErrorCode.ROUTER_COMM_ERROR, message, cause);
    }
    
    public RouterCommunicationException(Throwable cause) {
        super(ErrorCode.ROUTER_COMM_ERROR, cause);
    }
}
