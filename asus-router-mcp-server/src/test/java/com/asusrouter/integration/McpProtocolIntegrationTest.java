package com.asusrouter.integration;

import com.asusrouter.infrastructure.adapter.in.mcp.McpJsonRpcHandler;
import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcRequest;
import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MCP JSON-RPC protocol.
 * Tests the complete JSON-RPC 2.0 request/response cycle.
 * 
 * To enable: Set environment variable ROUTER_INTEGRATION_TEST=true
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ROUTER_INTEGRATION_TEST", matches = "true")
class McpProtocolIntegrationTest {
    
    @Autowired
    private McpJsonRpcHandler handler;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldListAllTools() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setMethod("tools/list");
        request.setParams(Map.of());
        request.setId("1");
        
        JsonRpcResponse response = handler.handleRequest(request);
        
        assertNotNull(response, "Response should not be null");
        assertEquals("2.0", response.getJsonrpc());
        assertEquals("1", response.getId());
        assertNull(response.getError(), "Error should be null for successful request");
        assertNotNull(response.getResult(), "Result should not be null");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        assertTrue(result.containsKey("tools"), "Result should contain tools list");
    }
    
    @Test
    void shouldGetUptimeViaJsonRpc() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setMethod("asus_router_get_uptime");
        request.setParams(Map.of());
        request.setId("2");
        
        JsonRpcResponse response = handler.handleRequest(request);
        
        assertNotNull(response);
        assertEquals("2.0", response.getJsonrpc());
        assertEquals("2", response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> uptime = (Map<String, Object>) response.getResult();
        assertTrue(uptime.containsKey("uptimeSeconds"), "Result should contain uptimeSeconds");
        assertTrue(uptime.containsKey("lastBootTime"), "Result should contain lastBootTime");
    }
    
    @Test
    void shouldGetMemoryUsageViaJsonRpc() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setMethod("asus_router_get_memory_usage");
        request.setParams(Map.of());
        request.setId("3");
        
        JsonRpcResponse response = handler.handleRequest(request);
        
        assertNotNull(response);
        assertNull(response.getError());
        assertNotNull(response.getResult());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> memory = (Map<String, Object>) response.getResult();
        assertTrue(memory.containsKey("totalMb"));
        assertTrue(memory.containsKey("usedMb"));
        assertTrue(memory.containsKey("freeMb"));
        assertTrue(memory.containsKey("usagePercent"));
    }
    
    @Test
    void shouldHandleInvalidMethod() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setMethod("invalid_method");
        request.setParams(Map.of());
        request.setId("4");
        
        JsonRpcResponse response = handler.handleRequest(request);
        
        assertNotNull(response);
        assertEquals("4", response.getId());
        assertNotNull(response.getError(), "Error should not be null for invalid method");
        assertNull(response.getResult(), "Result should be null for error response");
    }
    
    @Test
    void shouldHandleRequestWithoutId() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setMethod("asus_router_get_uptime");
        request.setParams(Map.of());
        // No ID - this is a notification
        
        assertTrue(request.isNotification(), "Request without ID should be notification");
        
        JsonRpcResponse response = handler.handleRequest(request);
        
        assertNotNull(response);
        assertNull(response.getId(), "Notification response should have null ID");
    }
    
    @Test
    void shouldHandleClientInfoWithMacParameter() {
        // This test requires a valid MAC address from your router
        // Skip if you don't have one
        String testMac = System.getenv("TEST_CLIENT_MAC");
        if (testMac == null) {
            testMac = "00:00:00:00:00:00"; // Will likely fail, but tests param handling
        }
        
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setMethod("asus_router_get_client_info_summary");
        request.setParams(Map.of("mac", testMac));
        request.setId("5");
        
        JsonRpcResponse response = handler.handleRequest(request);
        
        assertNotNull(response);
        assertEquals("5", response.getId());
        // Response might be error if MAC not found, that's OK for this test
        assertTrue(response.getResult() != null || response.getError() != null,
            "Response should have either result or error");
    }
}
