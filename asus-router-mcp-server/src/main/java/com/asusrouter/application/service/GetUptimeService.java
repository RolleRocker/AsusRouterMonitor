package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetUptimeUseCase;
import com.asusrouter.application.port.out.RouterUptimePort;
import com.asusrouter.domain.model.Uptime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case implementation for retrieving router uptime.
 * Implements hexagonal architecture by depending on ports, not concrete implementations.
 */
@Service
@RequiredArgsConstructor
public class GetUptimeService implements GetUptimeUseCase {
    
    private final RouterUptimePort routerUptimePort;
    
    @Override
    public Uptime execute() {
        String rawResponse = routerUptimePort.getUptime();
        return parseUptimeResponse(rawResponse);
    }
    
    /**
     * Parse router uptime response.
     * Expected format from router: "since;uptime" 
     * Example: "Thu, 22 Jul 2021 14:32:38 +0200;375001"
     */
    private Uptime parseUptimeResponse(String response) {
        String[] parts = response.split(";");
        if (parts.length != 2) {
            throw new IllegalStateException("Invalid uptime response format: " + response);
        }
        return new Uptime(parts[0].trim(), parts[1].trim());
    }
}
