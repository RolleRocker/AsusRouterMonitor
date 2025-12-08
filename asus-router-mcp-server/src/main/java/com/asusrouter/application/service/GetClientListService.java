package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetClientListUseCase;
import com.asusrouter.application.port.out.RouterClientListPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case implementation for retrieving client list in various formats.
 */
@Service
@RequiredArgsConstructor
public class GetClientListService implements GetClientListUseCase {
    
    private final RouterClientListPort routerClientListPort;
    
    @Override
    public String execute(Integer format) {
        // Normalize format parameter
        int normalizedFormat = (format != null) ? format : 0;
        
        // Validate format range
        if (normalizedFormat < 0 || normalizedFormat > 2) {
            normalizedFormat = 0;
        }
        
        // Return raw JSON response directly
        // Format 0 = basic list
        // Format 1 = with details
        // Format 2 = full JSON structure
        return routerClientListPort.getClientList(normalizedFormat);
    }
}
