package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetClientFullInfoUseCase;
import com.asusrouter.application.port.out.RouterClientListPort;
import com.asusrouter.domain.exception.ClientNotFoundException;
import com.asusrouter.domain.model.ClientFullInfo;
import com.asusrouter.domain.model.IpAddress;
import com.asusrouter.domain.model.MacAddress;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Iterator;

/**
 * Use case implementation for retrieving complete client information.
 */
@Service
@RequiredArgsConstructor
public class GetClientFullInfoService implements GetClientFullInfoUseCase {
    
    private final RouterClientListPort routerClientListPort;
    private final ObjectMapper objectMapper;
    
    @Override
    public ClientFullInfo execute(MacAddress mac) {
        String rawResponse = routerClientListPort.getClientList(2); // Format 2 = full JSON
        return parseClientFullInfo(rawResponse, mac);
    }
    
    /**
     * Parse full client info from JSON response.
     * Search for client by MAC address.
     */
    private ClientFullInfo parseClientFullInfo(String response, MacAddress targetMac) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String normalizedTargetMac = targetMac.normalized();
            
            // Client list is typically under a "get_clientlist" or similar key
            JsonNode clientsNode = root.path("get_clientlist");
            if (clientsNode.isMissingNode()) {
                clientsNode = root;
            }
            
            // Search through all clients
            Iterator<String> fieldNames = clientsNode.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                JsonNode clientNode = clientsNode.get(key);
                
                String clientMac = clientNode.path("mac").asText("");
                if (new MacAddress(clientMac).normalized().equals(normalizedTargetMac)) {
                    return buildClientFullInfo(clientNode);
                }
            }
            
            throw new ClientNotFoundException(targetMac.value(), 
                "Client with MAC address " + normalizedTargetMac + " not found");
            
        } catch (ClientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse client full info response", e);
        }
    }
    
    private ClientFullInfo buildClientFullInfo(JsonNode node) {
        return new ClientFullInfo(
            node.path("name").asText(""),
            node.path("nickName").asText(""),
            new IpAddress(node.path("ip").asText("0.0.0.0")),
            new MacAddress(node.path("mac").asText()),
            node.path("from").asText(""),
            node.path("macRepeat").asInt(1),
            node.path("isGateway").asBoolean(false),
            node.path("isWebStorage").asBoolean(false),
            node.path("isPrinter").asBoolean(false),
            node.path("isITunes").asBoolean(false),
            node.path("dpiType").asText(""),
            node.path("dpiDevice").asText(""),
            node.path("vendor").asText(""),
            node.path("osType").asText(""),
            node.path("ssid").asText(""),
            node.path("isWL").asInt(0),
            node.path("isOnline").asBoolean(false),
            node.path("rssi").asInt(0),
            node.path("curTx").asText(""),
            node.path("curRx").asText(""),
            node.path("totalTx").asText("0"),
            node.path("totalRx").asText("0"),
            node.path("wlConnectTime").asInt(0),
            node.path("ipMethod").asText(""),
            node.path("opMode").asInt(0),
            node.path("ROG").asBoolean(false),
            node.path("group").asText(""),
            node.path("callback").asText(""),
            node.path("keeparp").asText(""),
            node.path("qosLevel").asText(""),
            node.path("wtfast").asBoolean(false),
            node.path("internetMode").asText("allow"),
            node.path("internetState").asInt(0)
        );
    }
}
