package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetWanStatusUseCase;
import com.asusrouter.application.port.out.RouterWanLinkPort;
import com.asusrouter.domain.model.IpAddress;
import com.asusrouter.domain.model.Netmask;
import com.asusrouter.domain.model.WanStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Use case implementation for retrieving WAN connection status.
 */
@Service
@RequiredArgsConstructor
public class GetWanStatusService implements GetWanStatusUseCase {
    
    private final RouterWanLinkPort routerWanLinkPort;
    private final ObjectMapper objectMapper;
    
    @Override
    public WanStatus execute() {
        String rawResponse = routerWanLinkPort.getWanStatus();
        return parseWanStatusResponse(rawResponse);
    }
    
    /**
     * Parse WAN status JSON response.
     * Expected fields: status, statusCode, wanIP, gateway, netmask, dns
     */
    private WanStatus parseWanStatusResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            
            String status = root.path("status").asText("disconnected");
            int statusCode = root.path("statusCode").asInt(0);
            
            IpAddress ip = new IpAddress(root.path("wanIP").asText("0.0.0.0"));
            IpAddress gateway = new IpAddress(root.path("gateway").asText("0.0.0.0"));
            Netmask mask = new Netmask(root.path("netmask").asText("0.0.0.0"));
            
            List<IpAddress> dnsList = new ArrayList<>();
            JsonNode dnsNode = root.path("dns");
            if (dnsNode.isArray()) {
                for (JsonNode dnsEntry : dnsNode) {
                    dnsList.add(new IpAddress(dnsEntry.asText()));
                }
            } else {
                // Single DNS server as string
                String dnsStr = dnsNode.asText();
                if (!dnsStr.isEmpty() && !dnsStr.equals("0.0.0.0")) {
                    dnsList.add(new IpAddress(dnsStr));
                }
            }
            
            return new WanStatus(status, statusCode, ip, gateway, mask, dnsList);
            
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse WAN status response", e);
        }
    }
}
