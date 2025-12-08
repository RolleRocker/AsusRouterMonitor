package com.asusrouter.domain.exception;

/**
 * Exception thrown when a client with the specified MAC address is not found.
 */
public class ClientNotFoundException extends RouterException {
    private final String macAddress;
    
    public ClientNotFoundException(String macAddress) {
        super(ErrorCode.CLIENT_NOT_FOUND, 
              "Client with MAC address " + macAddress + " not found");
        this.macAddress = macAddress;
    }
    
    public ClientNotFoundException(String macAddress, String message) {
        super(ErrorCode.CLIENT_NOT_FOUND, message);
        this.macAddress = macAddress;
    }
    
    public String getMacAddress() {
        return macAddress;
    }
}
