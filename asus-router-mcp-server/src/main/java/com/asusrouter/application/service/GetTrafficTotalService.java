package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetTrafficTotalUseCase;
import com.asusrouter.application.port.out.RouterWanLinkPort;
import com.asusrouter.domain.model.TrafficTotal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case implementation for retrieving total network traffic.
 * Parses netdev statistics to calculate sent/received traffic in Megabits.
 */
@Service
@RequiredArgsConstructor
public class GetTrafficTotalService implements GetTrafficTotalUseCase {
    
    private final RouterWanLinkPort routerWanLinkPort;
    private final ObjectMapper objectMapper;
    
    @Override
    public TrafficTotal execute() {
        String rawResponse = routerWanLinkPort.getTrafficStats();
        return parseTrafficResponse(rawResponse);
    }
    
    /**
     * Parse traffic statistics from netdev JSON response.
     * Converts bytes to Megabits (1 byte = 8 bits, 1 Megabit = 1,000,000 bits).
     */
    private TrafficTotal parseTrafficResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            
            // WAN interface typically named 'eth0' or 'ppp0'
            JsonNode wanInterface = findWanInterface(root);
            
            long sentBytes = wanInterface.path("tx_bytes").asLong();
            long recvBytes = wanInterface.path("rx_bytes").asLong();
            
            // Convert to Megabits
            double sentMb = (sentBytes * 8.0) / 1_000_000.0;
            double recvMb = (recvBytes * 8.0) / 1_000_000.0;
            
            return new TrafficTotal(sentMb, recvMb);
            
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse traffic response", e);
        }
    }
    
    private JsonNode findWanInterface(JsonNode root) {
        // Try common WAN interface names
        String[] wanNames = {"eth0", "ppp0", "wan", "vlan2"};
        for (String name : wanNames) {
            if (root.has(name)) {
                return root.get(name);
            }
        }
        // Default to first interface if none match
        return root.elements().next();
    }
}
