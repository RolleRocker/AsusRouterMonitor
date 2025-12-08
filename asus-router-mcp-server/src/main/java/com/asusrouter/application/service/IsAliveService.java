package com.asusrouter.application.service;

import com.asusrouter.application.port.in.IsAliveUseCase;
import com.asusrouter.application.port.out.RouterUptimePort;
import com.asusrouter.domain.exception.RouterAuthenticationException;
import com.asusrouter.domain.exception.RouterCommunicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use case implementation for checking if router is alive/responsive.
 * Makes a simple request to verify connectivity and authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IsAliveService implements IsAliveUseCase {
    
    private final RouterUptimePort routerUptimePort;
    
    @Override
    public boolean execute() {
        try {
            // Try to get uptime - simplest request to verify router is responsive
            String response = routerUptimePort.getUptime();
            
            // Check if we got a valid response
            boolean isAlive = response != null && !response.isEmpty() && response.contains(";");
            
            log.debug("Router alive check: {}", isAlive);
            return isAlive;
            
        } catch (RouterAuthenticationException | RouterCommunicationException e) {
            log.warn("Router not alive: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during router alive check", e);
            return false;
        }
    }
}
