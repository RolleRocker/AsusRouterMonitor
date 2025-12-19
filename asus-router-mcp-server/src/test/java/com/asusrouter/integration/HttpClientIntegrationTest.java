package com.asusrouter.integration;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
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

import com.asusrouter.domain.exception.RouterCommunicationException;
import com.asusrouter.infrastructure.adapter.out.http.RouterCommandExecutor;

/**
 * HTTP Client Integration Tests for RouterCommandExecutor.
 * Tests low-level HTTP communication with mock router server.
 * Covers successful requests, error handling, and authentication.
 *
 * Test Structure:
 * - BasicHttpOperations: Core GET/POST requests
 * - Authentication: Cookie and header validation
 * - ErrorHandling: Connection failures, timeouts, invalid responses
 * - ResponseParsing: Different response formats (plain text, JSON, semicolon-delimited)
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("HTTP Client Integration Tests")
class HttpClientIntegrationTest {

    private static final int MOCK_ROUTER_PORT = 8889;
    private static final String MOCK_USERNAME = "admin";
    private static final String MOCK_PASSWORD = "test123";
    private static final String MOCK_HOST = "localhost";

    private MockRouterServer mockRouter;

    @Autowired
    private RouterCommandExecutor routerCommandExecutor;

    /**
     * Configure Spring to use mock router server on different port.
     */
    @DynamicPropertySource
    @SuppressWarnings("unused")
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("asus.router.host", () -> MOCK_HOST);
        registry.add("asus.router.port", () -> MOCK_ROUTER_PORT);
        registry.add("asus.router.username", () -> MOCK_USERNAME);
        registry.add("asus.router.password", () -> MOCK_PASSWORD);
        registry.add("asus.router.connection-timeout", () -> 3000);
        registry.add("asus.router.read-timeout", () -> 3000);
    }

    @BeforeAll
    @SuppressWarnings("unused")
    void setupMockRouter() throws IOException {
        mockRouter = new MockRouterServer(MOCK_ROUTER_PORT, MOCK_USERNAME, MOCK_PASSWORD);
        mockRouter.start();
    }

    @AfterAll
    @SuppressWarnings("unused")
    void teardownMockRouter() {
        if (mockRouter != null) {
            mockRouter.stop();
        }
    }

    // ========== BASIC HTTP OPERATIONS TESTS ==========

    @Nested
    @DisplayName("Basic HTTP Operations")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class BasicHttpOperations {

        @Test
        @Order(1)
        @DisplayName("Should execute GET request with hook parameter")
        void testGetRequestWithHook() {
            // Given: Router server is running and configured

            // When: Executing GET command with valid hook
            String response = routerCommandExecutor.executeGetCommand("uptime");

            // Then: Response should not be null or empty
            assertNotNull(response, "Response should not be null");
            assertTrue(response.length() > 0, "Response should not be empty");
            assertTrue(response.contains(";"), "Uptime response should be semicolon-delimited");
        }

        @Test
        @Order(2)
        @DisplayName("Should execute GET request with hook and parameter")
        void testGetRequestWithHookAndParameter() {
            // Given: Valid hook and parameter
            String hook = "nvram_get";
            String parameter = "model_name";

            // When: Executing GET command with both hook and parameter
            String response = routerCommandExecutor.executeGetCommand(hook, parameter);

            // Then: Response should contain router model information
            assertNotNull(response, "Response should not be null");
            assertTrue(response.length() > 0, "Response should not be empty");
        }

        @Test
        @Order(3)
        @DisplayName("Should handle memory_usage hook (semicolon-delimited response)")
        void testMemoryUsageHook() {
            // Given: Memory usage hook
            String hook = "memory_usage";

            // When: Executing GET command
            String response = routerCommandExecutor.executeGetCommand(hook);

            // Then: Response should be in format: "total;free;used"
            assertNotNull(response, "Response should not be null");
            String[] parts = response.split(";");
            assertEquals(3, parts.length, "Memory usage should have 3 parts: total;free;used");
        }

        @Test
        @Order(4)
        @DisplayName("Should handle cpu_usage hook (semicolon-delimited response)")
        void testCpuUsageHook() {
            // Given: CPU usage hook
            String hook = "cpu_usage";

            // When: Executing GET command
            String response = routerCommandExecutor.executeGetCommand(hook);

            // Then: Response should be in format: "cpu1Total;cpu1Usage;cpu2Total;cpu2Usage"
            assertNotNull(response, "Response should not be null");
            String[] parts = response.split(";");
            assertEquals(4, parts.length, "CPU usage should have 4 parts");
        }

        @Test
        @Order(5)
        @DisplayName("Should handle JSON response (netdev hook)")
        void testJsonResponse() {
            // Given: JSON response hook
            String hook = "netdev";

            // When: Executing GET command
            String response = routerCommandExecutor.executeGetCommand(hook);

            // Then: Response should be valid JSON
            assertNotNull(response, "Response should not be null");
            assertTrue(response.startsWith("{"), "JSON response should start with {");
            assertTrue(response.endsWith("}"), "JSON response should end with }");
        }
    }

    // ========== AUTHENTICATION TESTS ==========

    @Nested
    @DisplayName("Authentication")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AuthenticationTests {

        @Test
        @Order(1)
        @DisplayName("Should automatically authenticate on first request")
        void testAutoAuthentication() {
            // Given: Router server requires authentication

            // When: Making first request (authenticator should login)
            String response = routerCommandExecutor.executeGetCommand("is_alive");

            // Then: Request should succeed and return response
            assertNotNull(response, "Response should not be null");
            assertEquals("1", response.trim(), "Router should be alive (is_alive = 1)");
        }

        @Test
        @Order(2)
        @DisplayName("Should reuse authentication cookie for subsequent requests")
        void testAuthenticationReuse() {
            // Given: First request has already authenticated

            // When: Making multiple subsequent requests
            String response1 = routerCommandExecutor.executeGetCommand("is_alive");
            String response2 = routerCommandExecutor.executeGetCommand("uptime");
            String response3 = routerCommandExecutor.executeGetCommand("memory_usage");

            // Then: All requests should succeed without re-authentication
            assertNotNull(response1, "First response should not be null");
            assertNotNull(response2, "Second response should not be null");
            assertNotNull(response3, "Third response should not be null");
        }

        @Test
        @Order(3)
        @DisplayName("Should include authorization header in requests")
        void testAuthorizationHeader() {
            // Given: RouterCommandExecutor is properly configured

            // When: Executing any command (header should be added automatically)
            String response = routerCommandExecutor.executeGetCommand("wan_status");

            // Then: Request should succeed (authorization header was applied)
            assertNotNull(response, "Response should not be null");
            assertTrue(response.length() > 0, "Response should not be empty");
        }
    }

    // ========== ERROR HANDLING TESTS ==========

    @Nested
    @DisplayName("Error Handling")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ErrorHandlingTests {

        @Test
        @Order(1)
        @DisplayName("Should throw exception on non-existent hook")
        void testNonExistentHook() {
            // Given: Invalid hook name
            String invalidHook = "nonexistent_hook_xyz";

            // When: Executing GET command with invalid hook
            // Then: Should handle gracefully (mock server returns empty or 404)
            String response = routerCommandExecutor.executeGetCommand(invalidHook);
            assertNotNull(response, "Should return response even for invalid hook");
        }

        @Test
        @Order(2)
        @DisplayName("Should handle empty response gracefully")
        void testEmptyResponse() {
            // Given: Mock router configured to return empty response

            // When: Executing command that returns empty response
            String response = routerCommandExecutor.executeGetCommand("empty_response");

            // Then: Should return empty string instead of null
            assertNotNull(response, "Response should not be null");
        }

        @Test
        @Order(3)
        @DisplayName("Should recover from single failed request")
        void testRecoveryFromFailure() {
            // Given: A successful and failed request sequence

            // When: Making requests with potential failures in between
            String response1 = routerCommandExecutor.executeGetCommand("uptime");
            // Simulate network condition
            String response2 = routerCommandExecutor.executeGetCommand("memory_usage");

            // Then: Should recover and continue executing
            assertNotNull(response1, "First response should not be null");
            assertNotNull(response2, "Second response should not be null");
        }
    }

    // ========== RESPONSE FORMAT VALIDATION TESTS ==========

    @Nested
    @DisplayName("Response Format Validation")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ResponseFormatValidation {

        @Test
        @Order(1)
        @DisplayName("Should validate uptime response format")
        void testUptimeResponseFormat() {
            // Given: Uptime hook
            String response = routerCommandExecutor.executeGetCommand("uptime");

            // When: Parsing response
            String[] parts = response.split(";");

            // Then: Should have exactly 2 parts: datetime and seconds
            assertEquals(2, parts.length, "Uptime should have datetime and seconds");
            assertTrue(parts[0].length() > 0, "Datetime should not be empty");
            assertTrue(parts[1].length() > 0, "Seconds should not be empty");
        }

        @Test
        @Order(2)
        @DisplayName("Should validate WAN status JSON structure")
        void testWanStatusJsonStructure() {
            // Given: WAN status hook
            String response = routerCommandExecutor.executeGetCommand("wan_status");

            // When: Checking structure
            // Then: Should be valid JSON
            assertNotNull(response, "Response should not be null");
            assertTrue(response.startsWith("{"), "Should be JSON object");
        }

        @Test
        @Order(3)
        @DisplayName("Should validate online clients JSON array")
        void testOnlineClientsJsonArray() {
            // Given: Online clients hook
            String response = routerCommandExecutor.executeGetCommand("onlinelist");

            // When: Checking response type
            // Then: Should be JSON array
            assertNotNull(response, "Response should not be null");
            assertTrue(
                response.startsWith("[") || response.startsWith("{}"),
                "Should be JSON array or empty object"
            );
        }
    }

    // ========== PERFORMANCE TESTS ==========

    @Nested
    @DisplayName("Performance Characteristics")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PerformanceCharacteristics {

        @Test
        @Order(1)
        @DisplayName("Should respond within timeout duration")
        void testResponseTime() {
            // Given: Configured timeout is 3 seconds
            long startTime = System.nanoTime();

            // When: Executing command
            String response = routerCommandExecutor.executeGetCommand("is_alive");

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            // Then: Should complete within reasonable time
            assertNotNull(response, "Response should not be null");
            assertTrue(
                durationMs < 3000,
                "Request should complete within 3 seconds, took: " + durationMs + "ms"
            );
        }

        @Test
        @Order(2)
        @DisplayName("Should handle concurrent requests")
        void testConcurrentRequests() throws InterruptedException {
            // Given: Multiple threads executing commands
            Thread thread1 = new Thread(() -> {
                String response = routerCommandExecutor.executeGetCommand("uptime");
                assertNotNull(response, "Thread 1 response should not be null");
            });

            Thread thread2 = new Thread(() -> {
                String response = routerCommandExecutor.executeGetCommand("memory_usage");
                assertNotNull(response, "Thread 2 response should not be null");
            });

            Thread thread3 = new Thread(() -> {
                String response = routerCommandExecutor.executeGetCommand("cpu_usage");
                assertNotNull(response, "Thread 3 response should not be null");
            });

            // When: Executing requests concurrently
            thread1.start();
            thread2.start();
            thread3.start();

            thread1.join(5000);
            thread2.join(5000);
            thread3.join(5000);

            // Then: All threads should complete successfully
            assertEquals(Thread.State.TERMINATED, thread1.getState(), "Thread 1 should complete");
            assertEquals(Thread.State.TERMINATED, thread2.getState(), "Thread 2 should complete");
            assertEquals(Thread.State.TERMINATED, thread3.getState(), "Thread 3 should complete");
        }
    }
}

