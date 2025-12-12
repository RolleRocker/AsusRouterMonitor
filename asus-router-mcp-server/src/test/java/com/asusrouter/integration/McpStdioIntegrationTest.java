package com.asusrouter.integration;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Integration tests for MCP stdio transport.
 * Tests complete stdin/stdout communication cycle.
 * 
 * NOTE: These tests are currently disabled as they require stdio redirection
 * which is not yet implemented. The simulateStdioRequest() method needs
 * proper implementation with System.in/System.out mocking.
 */
@Disabled("Stdio mocking not yet implemented - requires System.in/out redirection")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class McpStdioIntegrationTest {
    
    private static final int MOCK_ROUTER_PORT = 9890; // Changed from 8890 to avoid conflicts
    private MockRouterServer mockRouter;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("asus.router.host", () -> "localhost");
        registry.add("asus.router.port", () -> MOCK_ROUTER_PORT);
        registry.add("asus.router.username", () -> "admin");
        registry.add("asus.router.password", () -> "test123");
    }
    
    @BeforeAll
    void startMockRouter() throws IOException {
        mockRouter = new MockRouterServer(MOCK_ROUTER_PORT, "admin", "test123");
        mockRouter.start();
    }
    
    @AfterAll
    void stopMockRouter() {
        if (mockRouter != null) {
            mockRouter.stop();
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("STDIO-1: Test tools/list request via stdio")
    void testToolsListViaStdio() throws Exception {
        String jsonRequest = "{\"jsonrpc\":\"2.0\",\"method\":\"tools/list\",\"params\":{},\"id\":\"1\"}";
        
        String response = simulateStdioRequest(jsonRequest);
        
        assertNotNull(response, "Response should not be null");
        assertTrue(response.contains("\"jsonrpc\":\"2.0\""), "Should be JSON-RPC 2.0");
        assertTrue(response.contains("\"id\":\"1\""), "Should have matching ID");
        assertTrue(response.contains("\"result\""), "Should have result");
        assertTrue(response.contains("tools"), "Should contain tools list");
    }
    
    @Test
    @Order(2)
    @DisplayName("STDIO-2: Test uptime request via stdio")
    void testUptimeViaStdio() throws Exception {
        String jsonRequest = "{\"jsonrpc\":\"2.0\",\"method\":\"asus_router_get_uptime\",\"params\":{},\"id\":\"2\"}";
        
        String response = simulateStdioRequest(jsonRequest);
        
        assertNotNull(response, "Response should not be null");
        assertTrue(response.contains("since"), "Should contain uptime since");
        assertTrue(response.contains("uptime"), "Should contain uptime value");
    }
    
    @Test
    @Order(3)
    @DisplayName("STDIO-3: Test is_alive request via stdio")
    void testIsAliveViaStdio() throws Exception {
        String jsonRequest = "{\"jsonrpc\":\"2.0\",\"method\":\"asus_router_is_alive\",\"params\":{},\"id\":\"3\"}";
        
        String response = simulateStdioRequest(jsonRequest);
        
        assertNotNull(response, "Response should not be null");
        assertTrue(response.contains("true") || response.contains("result"), 
            "Should indicate router is alive");
    }
    
    @Test
    @Order(4)
    @DisplayName("STDIO-4: Test request with parameters via stdio")
    void testRequestWithParametersViaStdio() throws Exception {
        String jsonRequest = "{\"jsonrpc\":\"2.0\",\"method\":\"asus_router_get_nvram\"," +
                           "\"params\":{\"nvram_command\":\"lan_ipaddr\"},\"id\":\"4\"}";
        
        String response = simulateStdioRequest(jsonRequest);
        
        assertNotNull(response, "Response should not be null");
        assertTrue(response.contains("192.168.1.1") || response.contains("result"), 
            "Should return LAN IP");
    }
    
    @Test
    @Order(5)
    @DisplayName("STDIO-5: Test error handling via stdio")
    void testErrorHandlingViaStdio() throws Exception {
        String jsonRequest = "{\"jsonrpc\":\"2.0\",\"method\":\"invalid_method\",\"params\":{},\"id\":\"5\"}";
        
        String response = simulateStdioRequest(jsonRequest);
        
        assertNotNull(response, "Response should not be null");
        assertTrue(response.contains("error"), "Should contain error");
        assertTrue(response.contains("\"id\":\"5\""), "Should have matching ID");
    }
    
    @Test
    @Order(6)
    @DisplayName("STDIO-6: Test malformed JSON via stdio")
    void testMalformedJsonViaStdio() throws Exception {
        String jsonRequest = "{invalid json}";
        
        String response = simulateStdioRequest(jsonRequest);
        
        assertNotNull(response, "Response should not be null");
        assertTrue(response.contains("error") || response.contains("parse"), 
            "Should indicate parse error");
    }
    
    /**
     * Simulate stdin/stdout request processing.
     * In real MCP usage, this would be handled by the transport layer.
     */
    private String simulateStdioRequest(String jsonRequest) throws Exception {
        // This is a simplified simulation
        // In practice, McpStdioTransport reads from System.in and writes to System.out
        // For testing, we would need to mock or redirect stdio streams
        
        // For now, return a mock response format
        // TODO: Implement actual stdio redirection for full integration test
        return "{\"jsonrpc\":\"2.0\",\"result\":{},\"id\":\"test\"}";
    }
}
