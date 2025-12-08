package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetClientInfoSummaryUseCase;
import com.asusrouter.application.port.out.RouterClientListPort;
import com.asusrouter.domain.exception.ClientNotFoundException;
import com.asusrouter.domain.model.ClientSummary;
import com.asusrouter.domain.model.IpAddress;
import com.asusrouter.domain.model.MacAddress;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Iterator;

/**
 * Use case implementation for retrieving client summary information.
 */
@Service
@RequiredArgsConstructor
public class GetClientInfoSummaryService implements GetClientInfoSummaryUseCase {
    
    private final RouterClientListPort routerClientListPort;
    private final ObjectMapper objectMapper;
    
    @Override
    public ClientSummary execute(MacAddress mac) {
        String rawResponse = routerClientListPort.getClientList(1); // Format 1 = with details
        return parseClientSummary(rawResponse, mac);
    }
    
    /**
     * Parse client summary from JSON response.
     */
    private ClientSummary parseClientSummary(String response, MacAddress targetMac) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String normalizedTargetMac = targetMac.normalized();
            
            JsonNode clientsNode = root.path("get_clientlist");
            if (clientsNode.isMissingNode()) {
                clientsNode = root;
            }
            
            Iterator<String> fieldNames = clientsNode.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                JsonNode clientNode = clientsNode.get(key);
                
                String clientMac = clientNode.path("mac").asText("");
                if (new MacAddress(clientMac).normalized().equals(normalizedTargetMac)) {
                    return buildClientSummary(clientNode);
                }
            }
            
            throw new ClientNotFoundException(targetMac.value(),
                "Client with MAC address " + normalizedTargetMac + " not found");
            
        } catch (ClientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse client summary response", e);
        }
    }
    
    private ClientSummary buildClientSummary(JsonNode node) {
        return new ClientSummary(
            node.path("nickName").asText(""),
            new IpAddress(node.path("ip").asText("0.0.0.0")),
            new MacAddress(node.path("mac").asText()),
            node.path("isOnline").asBoolean(false),
            node.path("name").asText(""),
            node.path("vendor").asText(""),
            node.path("isWL").asInt(0),
            node.path("rssi").asInt(0),
            node.path("curTx").asText("")
        );
    }
}
