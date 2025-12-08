package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetOnlineClientsUseCase;
import com.asusrouter.application.port.out.RouterClientListPort;
import com.asusrouter.domain.model.IpAddress;
import com.asusrouter.domain.model.MacAddress;
import com.asusrouter.domain.model.OnlineClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Use case implementation for retrieving online clients list.
 */
@Service
@RequiredArgsConstructor
public class GetOnlineClientsService implements GetOnlineClientsUseCase {
    
    private final RouterClientListPort routerClientListPort;
    private final ObjectMapper objectMapper;
    
    @Override
    public List<OnlineClient> execute() {
        String rawResponse = routerClientListPort.getOnlineClients();
        return parseOnlineClientsResponse(rawResponse);
    }
    
    /**
     * Parse online clients JSON response.
     * Expected format: array of objects with 'mac' and 'ip' fields.
     */
    private List<OnlineClient> parseOnlineClientsResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            List<OnlineClient> clients = new ArrayList<>();
            
            if (root.isArray()) {
                for (JsonNode clientNode : root) {
                    String macStr = clientNode.path("mac").asText();
                    String ipStr = clientNode.path("ip").asText();
                    
                    if (!macStr.isEmpty() && !ipStr.isEmpty()) {
                        MacAddress mac = new MacAddress(macStr);
                        IpAddress ip = new IpAddress(ipStr);
                        clients.add(new OnlineClient(mac, ip));
                    }
                }
            }
            
            return clients;
            
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse online clients response", e);
        }
    }
}
