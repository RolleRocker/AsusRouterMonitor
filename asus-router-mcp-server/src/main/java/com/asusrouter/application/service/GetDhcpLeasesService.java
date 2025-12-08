package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetDhcpLeasesUseCase;
import com.asusrouter.application.port.out.RouterDhcpPort;
import com.asusrouter.domain.model.DhcpLease;
import com.asusrouter.domain.model.IpAddress;
import com.asusrouter.domain.model.MacAddress;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Use case implementation for retrieving DHCP lease table.
 */
@Service
@RequiredArgsConstructor
public class GetDhcpLeasesService implements GetDhcpLeasesUseCase {
    
    private final RouterDhcpPort routerDhcpPort;
    private final ObjectMapper objectMapper;
    
    @Override
    public List<DhcpLease> execute() {
        String rawResponse = routerDhcpPort.getDhcpLeases();
        return parseDhcpLeases(rawResponse);
    }
    
    /**
     * Parse DHCP leases from JSON response.
     * Expected format: array of lease objects or semicolon-separated string.
     */
    private List<DhcpLease> parseDhcpLeases(String response) {
        try {
            // Try JSON format first
            if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                return parseJsonFormat(response);
            }
            
            // Fallback to semicolon-separated format
            return parseSemicolonFormat(response);
            
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse DHCP leases response", e);
        }
    }
    
    private List<DhcpLease> parseJsonFormat(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        List<DhcpLease> leases = new ArrayList<>();
        
        JsonNode leasesNode = root.isArray() ? root : root.path("leases");
        
        for (JsonNode leaseNode : leasesNode) {
            String hostname = leaseNode.path("hostname").asText("");
            String mac = leaseNode.path("mac").asText();
            String ip = leaseNode.path("ip").asText();
            String expires = leaseNode.path("expires").asText("0");
            
            if (!mac.isEmpty() && !ip.isEmpty()) {
                leases.add(new DhcpLease(
                    hostname,
                    new MacAddress(mac),
                    new IpAddress(ip),
                    expires
                ));
            }
        }
        
        return leases;
    }
    
    private List<DhcpLease> parseSemicolonFormat(String response) {
        List<DhcpLease> leases = new ArrayList<>();
        String[] lines = response.split("\n");
        
        for (String line : lines) {
            String[] parts = line.split(";");
            if (parts.length >= 3) {
                String hostname = parts.length > 3 ? parts[3] : "";
                leases.add(new DhcpLease(
                    hostname,
                    new MacAddress(parts[1]),
                    new IpAddress(parts[2]),
                    parts[0]
                ));
            }
        }
        
        return leases;
    }
}
