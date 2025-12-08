package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetCpuUsageUseCase;
import com.asusrouter.application.port.out.RouterCpuPort;
import com.asusrouter.domain.model.CpuUsage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case implementation for retrieving router CPU usage.
 */
@Service
@RequiredArgsConstructor
public class GetCpuUsageService implements GetCpuUsageUseCase {
    
    private final RouterCpuPort routerCpuPort;
    
    @Override
    public CpuUsage execute() {
        String rawResponse = routerCpuPort.getCpuUsage();
        return parseCpuUsageResponse(rawResponse);
    }
    
    /**
     * Parse router CPU usage response.
     * Expected format: "cpu1Total;cpu1Usage;cpu2Total;cpu2Usage"
     * Example: "38106047;3395512;38106008;2384694"
     */
    private CpuUsage parseCpuUsageResponse(String response) {
        String[] parts = response.split(";");
        if (parts.length != 4) {
            throw new IllegalStateException("Invalid CPU usage response format: " + response);
        }
        return new CpuUsage(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
    }
}
