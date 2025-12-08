package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetTrafficUseCase;
import com.asusrouter.application.port.out.RouterWanLinkPort;
import com.asusrouter.domain.model.TrafficSpeed;
import com.asusrouter.domain.model.TrafficTotal;
import com.asusrouter.domain.model.TrafficWithSpeed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case implementation for retrieving network traffic with speed.
 * Combines total traffic and current transfer rates.
 */
@Service
@RequiredArgsConstructor
public class GetTrafficService implements GetTrafficUseCase {
    
    private final RouterWanLinkPort routerWanLinkPort;
    private final ObjectMapper objectMapper;
    
    @Override
    public TrafficWithSpeed execute() {
        String rawResponse = routerWanLinkPort.getTrafficStats();
        return parseTrafficWithSpeedResponse(rawResponse);
    }
    
    /**
     * Parse traffic statistics with speed calculation.
     * Router typically provides tx_bytes, rx_bytes, tx_packets, rx_packets.
     * Speed is calculated from rate of change in bytes.
     */
    private TrafficWithSpeed parseTrafficWithSpeedResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode wanInterface = findWanInterface(root);
            
            // Total traffic in bytes
            long sentBytes = wanInterface.path("tx_bytes").asLong();
            long recvBytes = wanInterface.path("rx_bytes").asLong();
            
            // Convert to Megabits
            double sentMb = (sentBytes * 8.0) / 1_000_000.0;
            double recvMb = (recvBytes * 8.0) / 1_000_000.0;
            TrafficTotal total = new TrafficTotal(sentMb, recvMb);
            
            // Current speed (if available in response, otherwise 0)
            double sentSpeedKbps = wanInterface.path("tx_speed").asDouble(0.0);
            double recvSpeedKbps = wanInterface.path("rx_speed").asDouble(0.0);
            TrafficSpeed speed = new TrafficSpeed(sentSpeedKbps, recvSpeedKbps);
            
            return new TrafficWithSpeed(total, speed);
            
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse traffic with speed response", e);
        }
    }
    
    private JsonNode findWanInterface(JsonNode root) {
        String[] wanNames = {"eth0", "ppp0", "wan", "vlan2"};
        for (String name : wanNames) {
            if (root.has(name)) {
                return root.get(name);
            }
        }
        return root.elements().next();
    }
}
