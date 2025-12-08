package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetMemoryUsageUseCase;
import com.asusrouter.application.port.out.RouterMemoryPort;
import com.asusrouter.domain.model.MemoryUsage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case implementation for retrieving router memory usage.
 */
@Service
@RequiredArgsConstructor
public class GetMemoryUsageService implements GetMemoryUsageUseCase {
    
    private final RouterMemoryPort routerMemoryPort;
    
    @Override
    public MemoryUsage execute() {
        String rawResponse = routerMemoryPort.getMemoryUsage();
        return parseMemoryUsageResponse(rawResponse);
    }
    
    /**
     * Parse router memory usage response.
     * Expected format: "memTotal;memFree;memUsed"
     * Example: "262144;107320;154824"
     */
    private MemoryUsage parseMemoryUsageResponse(String response) {
        String[] parts = response.split(";");
        if (parts.length != 3) {
            throw new IllegalStateException("Invalid memory usage response format: " + response);
        }
        return new MemoryUsage(parts[0].trim(), parts[1].trim(), parts[2].trim());
    }
}
