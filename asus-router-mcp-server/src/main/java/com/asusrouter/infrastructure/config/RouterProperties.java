package com.asusrouter.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for ASUS Router connection.
 * Maps to application.yml properties under 'asus.router'.
 */
@Configuration
@ConfigurationProperties(prefix = "asus.router")
@Data
public class RouterProperties {
    
    /**
     * Router hostname or IP address.
     */
    private String host = "192.168.1.1";
    
    /**
     * Router HTTP/HTTPS port.
     */
    private int port = 80;
    
    /**
     * Use HTTPS connection.
     */
    private boolean useHttps = false;
    
    /**
     * Router admin username.
     */
    private String username = "admin";
    
    /**
     * Router admin password.
     */
    private String password;
    
    /**
     * Connection timeout in milliseconds.
     */
    private int connectionTimeout = 5000;
    
    /**
     * Read timeout in milliseconds.
     */
    private int readTimeout = 10000;
    
    /**
     * Get base URL for router.
     */
    public String getBaseUrl() {
        String protocol = useHttps ? "https" : "http";
        return String.format("%s://%s:%d", protocol, host, port);
    }
}
