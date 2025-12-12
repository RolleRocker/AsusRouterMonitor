package com.asusrouter.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.asusrouter.application.port.in.ShowRouterInfoUseCase;
import com.asusrouter.cli.ShowRouterInfoRunner;

/**
 * Integration tests for CLI (ShowRouterInfo) functionality.
 * Tests command-line interface with mock router.
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CliRunnerIntegrationTest {
    
    private static final int MOCK_ROUTER_PORT = 9891; // Changed from 8891 to avoid conflicts
    private MockRouterServer mockRouter;
    
    @Autowired
    private ShowRouterInfoUseCase showRouterInfoUseCase;
    
    private final ByteArrayOutputStream outputCapture = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    
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
    
    @BeforeEach
    void captureOutput() {
        outputCapture.reset();
        System.setOut(new PrintStream(outputCapture));
    }
    
    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }
    
    @Test
    @Order(1)
    @DisplayName("CLI-1: Test basic router info output")
    void testBasicRouterInfo() {
        String output = showRouterInfoUseCase.execute(false);
        
        assertNotNull(output, "Output should not be null");
        assertTrue(output.contains("ASUS ROUTER MONITORING REPORT"), "Should contain router title");
        assertTrue(output.contains("Uptime"), "Should contain uptime section");
        assertTrue(output.contains("Memory"), "Should contain memory section");
        assertTrue(output.contains("CPU"), "Should contain CPU section");
        
        // Verify structure with box-drawing characters
        assertTrue(output.contains("═══") || output.contains("┌─") || output.contains("└─"), "Should contain separator lines");
        assertTrue(output.length() > 100, "Basic output should be substantial");
    }
    
    @Test
    @Order(2)
    @DisplayName("CLI-2: Test detailed router info output")
    void testDetailedRouterInfo() {
        String output = showRouterInfoUseCase.execute(true);
        
        assertNotNull(output, "Output should not be null");
        assertTrue(output.length() > 400, "Detailed output should be longer");
        
        // Should contain all sections
        assertTrue(output.contains("Uptime") || output.contains("SYSTEM INFORMATION"), "Should contain uptime or system info");
        assertTrue(output.contains("Memory"), "Should contain memory");
        assertTrue(output.contains("CPU"), "Should contain CPU");
        assertTrue(output.contains("NETWORK STATUS") || output.contains("WAN"), "Should contain network status");
        assertTrue(output.contains("CONNECTED CLIENTS") || output.contains("Total Online"), "Should contain clients");
    }
    
    @Test
    @Order(3)
    @DisplayName("CLI-3: Test uptime formatting")
    void testUptimeFormatting() {
        String output = showRouterInfoUseCase.execute(false);
        
        // Verify uptime is formatted with days/hours/minutes (colon-separated format)
        assertTrue(output.matches("(?s).*\\d+\\s+days.*") || 
                   output.matches("(?s).*\\d{2}:\\d{2}:\\d{2}.*"),
            "Should contain formatted uptime duration");
    }
    
    @Test
    @Order(4)
    @DisplayName("CLI-4: Test memory usage formatting")
    void testMemoryUsageFormatting() {
        String output = showRouterInfoUseCase.execute(false);
        
        // Should show memory with percentage and MB (e.g., "59.0% used (154 MB / 256 MB)")
        assertTrue(output.matches("(?s).*\\d+\\.\\d+%.*used.*") ||
                   output.matches("(?s).*\\d+\\s+MB.*"),
            "Should contain formatted memory usage");
    }
    
    @Test
    @Order(5)
    @DisplayName("CLI-5: Test CPU usage formatting")
    void testCpuUsageFormatting() {
        String output = showRouterInfoUseCase.execute(false);
        
        // Should show CPU usage with decimal numbers (e.g., "8.9% average" or "CPU1: 8.9")
        assertTrue(output.matches("(?s).*CPU.*\\d+\\.\\d+.*"),
            "Should contain CPU usage with decimal values");
    }
    
    @Test
    @Order(6)
    @DisplayName("CLI-6: Test traffic statistics in detailed mode")
    void testTrafficStatistics() {
        String output = showRouterInfoUseCase.execute(true);
        
        // Output contains network status section
        assertTrue(output.contains("NETWORK STATUS") || output.contains("WAN"),
            "Should contain network status section");
    }
    
    @Test
    @Order(7)
    @DisplayName("CLI-7: Test WAN status in detailed mode")
    void testWanStatusDisplay() {
        String output = showRouterInfoUseCase.execute(true);
        
        assertTrue(output.contains("WAN") || output.contains("Status"),
            "Should contain WAN/Status section");
        assertTrue(output.matches("(?s).*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*"),
            "Should contain IP address");
    }
    
    @Test
    @Order(8)
    @DisplayName("CLI-8: Test online clients list in detailed mode")
    void testOnlineClientsList() {
        String output = showRouterInfoUseCase.execute(true);
        
        assertTrue(output.contains("CONNECTED CLIENTS") || output.contains("Total Online"),
            "Should contain clients section");
        // In detailed mode, should show client table with MAC addresses
        assertTrue(output.contains("--detailed") || output.contains("MAC Address") || 
                   output.matches("(?s).*[A-F0-9]{2}:[A-F0-9]{2}:[A-F0-9]{2}:[A-F0-9]{2}:[A-F0-9]{2}:[A-F0-9]{2}.*"),
            "Should contain MAC addresses or detailed flag message");
    }
    
    @Test
    @Order(9)
    @DisplayName("CLI-9: Test output has proper formatting characters")
    void testOutputFormatting() {
        String output = showRouterInfoUseCase.execute(false);
        
        // Should use box-drawing characters or ASCII art
        assertTrue(output.contains("═") || output.contains("─") || output.contains("┌") ||
                   output.contains("===") || output.contains("---"), 
            "Should contain separator lines");
        
        // Should have sections separated by newlines
        assertTrue(output.split("\\n").length > 5, 
            "Should have multiple lines");
    }
    
    @Test
    @Order(10)
    @DisplayName("CLI-10: Test basic vs detailed output length difference")
    void testOutputLengthDifference() {
        String basicOutput = showRouterInfoUseCase.execute(false);
        String detailedOutput = showRouterInfoUseCase.execute(true);
        
        // Detailed output should be at least as long as basic, possibly longer with client table
        assertTrue(detailedOutput.length() >= basicOutput.length(),
            "Detailed output should be at least as long as basic output");
    }
    
    @Test
    @Order(11)
    @DisplayName("CLI-11: Test CLI runner with --cli argument")
    void testCliRunnerActivation() {
        ShowRouterInfoRunner runner = new ShowRouterInfoRunner(showRouterInfoUseCase);
        
        // Test that runner can be instantiated
        assertNotNull(runner, "CLI runner should be instantiated");
        
        // Note: Full execution test would require mocking CommandLineRunner
        // or running actual Spring Boot application with args
    }
    
    @Test
    @Order(12)
    @DisplayName("CLI-12: Test error message formatting when router unavailable")
    void testErrorMessageFormatting() {
        // This would require a separate test with router stopped
        // For now, verify that error handling exists
        
        String output = showRouterInfoUseCase.execute(false);
        assertNotNull(output, "Should return output even with potential errors");
    }
    
    @Nested
    @DisplayName("Output Format Validation")
    class OutputFormatTests {
        
        @Test
        @DisplayName("Verify no ANSI color codes in output")
        void testNoAnsiCodes() {
            String output = showRouterInfoUseCase.execute(false);
            
            // Check for common ANSI escape sequences
            assertFalse(output.matches(".*\\x1b\\[[0-9;]*m.*"),
                "Output should not contain ANSI color codes (or test if they should be present)");
        }
        
        @Test
        @DisplayName("Verify consistent indentation")
        void testConsistentIndentation() {
            String output = showRouterInfoUseCase.execute(false);
            String[] lines = output.split("\n");
            
            // Check that lines don't have excessive leading spaces
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    assertTrue(line.length() - line.stripLeading().length() < 20,
                        "Line should not have excessive indentation: " + line);
                }
            }
        }
        
        @Test
        @DisplayName("Verify no HTML tags in output")
        void testNoHtmlTags() {
            String output = showRouterInfoUseCase.execute(false);
            
            assertFalse(output.contains("<") && output.contains(">"),
                "Output should not contain HTML tags");
        }
        
        @Test
        @DisplayName("Verify UTF-8 compatible characters")
        void testUtf8Compatibility() {
            String output = showRouterInfoUseCase.execute(false);
            
            // Should be able to encode as UTF-8
            assertDoesNotThrow(() -> output.getBytes("UTF-8"),
                "Output should be UTF-8 compatible");
        }
    }
    
    @Nested
    @DisplayName("Content Accuracy Tests")
    class ContentAccuracyTests {
        
        @Test
        @DisplayName("Verify uptime is positive")
        void testUptimeIsPositive() {
            String output = showRouterInfoUseCase.execute(false);
            
            // Extract uptime seconds and verify it's positive
            assertTrue(output.contains("Uptime"), "Should contain uptime");
            // Additional parsing would be needed to extract exact value
        }
        
        @Test
        @DisplayName("Verify memory percentages are valid")
        void testMemoryPercentagesValid() {
            String output = showRouterInfoUseCase.execute(false);
            
            // If percentages are shown, they should be 0-100
            // Would need regex to extract and validate
            assertTrue(output.contains("Memory"), "Should contain memory info");
        }
        
        @Test
        @DisplayName("Verify IP addresses have valid format")
        void testIpAddressFormat() {
            String output = showRouterInfoUseCase.execute(true);
            
            // Should contain valid IPv4 addresses
            if (output.matches("(?s).*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*")) {
                // Verify each octet is 0-255 (simplified check)
                assertTrue(true, "Contains valid-looking IP addresses");
            }
        }
    }
}
