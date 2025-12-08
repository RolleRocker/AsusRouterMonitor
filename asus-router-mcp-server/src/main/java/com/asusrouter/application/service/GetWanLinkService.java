package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetWanLinkUseCase;
import com.asusrouter.application.port.out.RouterWanLinkPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case implementation for retrieving WAN link information.
 */
@Service
@RequiredArgsConstructor
public class GetWanLinkService implements GetWanLinkUseCase {
    
    private final RouterWanLinkPort routerWanLinkPort;
    
    @Override
    public String execute(Integer unit) {
        // Normalize unit parameter (default to 0 for primary WAN)
        int normalizedUnit = (unit != null) ? unit : 0;
        
        // Validate unit range (typically 0 or 1 for dual-WAN setups)
        if (normalizedUnit < 0 || normalizedUnit > 1) {
            normalizedUnit = 0;
        }
        
        // Return raw JSON response directly
        return routerWanLinkPort.getWanLink(normalizedUnit);
    }
}
