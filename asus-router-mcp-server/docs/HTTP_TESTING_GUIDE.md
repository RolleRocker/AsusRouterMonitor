# HTTP Testing Guide for ASUS Router MCP Server

## Overview

HTTP testing in this project focuses on validating the low-level HTTP communication layer between the Spring Boot application and the ASUS router. The `RouterCommandExecutor` is the central component that handles all HTTP requests.

## Test Files

### 1. **HttpClientIntegrationTest.java** (NEW)
Complete HTTP testing suite with 18+ tests organized in 5 nested test classes.

**Location:** `src/test/java/com/asusrouter/integration/HttpClientIntegrationTest.java`

### 2. **MockRouterServer.java** (EXISTING)
Mock HTTP server that simulates ASUS router endpoints for testing without a physical router.

**Location:** `src/test/java/com/asusrouter/integration/MockRouterServer.java`

### 3. **RouterToolsIntegrationTest.java** (EXISTING)
Higher-level integration tests that test business logic through use cases.

**Location:** `src/test/java/com/asusrouter/integration/RouterToolsIntegrationTest.java`

## Architecture: Three Layers of HTTP Testing

```
┌─────────────────────────────────────────────────────────┐
│ Layer 3: Business Logic Tests (RouterToolsIntegrationTest)
│ Tests: Use case services with domain model responses
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│ Layer 2: HTTP Client Tests (HttpClientIntegrationTest)  │
│ Tests: RouterCommandExecutor with raw string responses
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│ Layer 1: Mock Server (MockRouterServer)                 │
│ Simulates: ASUS router HTTP API endpoints
└─────────────────────────────────────────────────────────┘
```

## HTTP Test Components

### RouterCommandExecutor (Component Under Test)
- **Location:** `src/main/java/.../infrastructure/adapter/out/http/RouterCommandExecutor.java`
- **Responsibility:** Execute HTTP GET/POST requests to router API
- **Key Methods:**
  - `executeGetCommand(String hook)` - Basic GET request
  - `executeGetCommand(String hook, String parameter)` - GET with parameter
  - `executePostCommand(String hook, String data)` - POST request (if implemented)

### AsusRouterAuthenticator (Dependency)
- **Responsibility:** Handle authentication and session management
- **Key Methods:**
  - `getAuthorizationHeader()` - Provides authentication credentials
  - `authenticate()` - Performs initial login to router

### MockRouterServer (Test Infrastructure)
Mock HTTP server running on port 8889 (configurable) that simulates:
- `/login.cgi` - Authentication endpoint
- `/appGet.cgi` - Main API endpoint with hook parameters

## Test Organization

### HttpClientIntegrationTest Structure

The test class is organized into 5 nested test classes:

#### 1. BasicHttpOperations (5 tests)
Tests core GET request functionality:
```java
@Nested
class BasicHttpOperations {
    @Test void testGetRequestWithHook()
    @Test void testGetRequestWithHookAndParameter()
    @Test void testMemoryUsageHook()
    @Test void testCpuUsageHook()
    @Test void testJsonResponse()
}
```

**What's Tested:**
- Simple GET request with hook only
- GET request with hook and parameter
- Semicolon-delimited response parsing
- JSON response handling

#### 2. AuthenticationTests (3 tests)
Tests authentication and session management:
```java
@Nested
class AuthenticationTests {
    @Test void testAutoAuthentication()
    @Test void testAuthenticationReuse()
    @Test void testAuthorizationHeader()
}
```

**What's Tested:**
- Automatic login on first request
- Cookie reuse for subsequent requests
- Authorization header injection

#### 3. ErrorHandlingTests (3 tests)
Tests error scenarios and recovery:
```java
@Nested
class ErrorHandlingTests {
    @Test void testNonExistentHook()
    @Test void testEmptyResponse()
    @Test void testRecoveryFromFailure()
}
```

**What's Tested:**
- Invalid hook handling
- Empty response handling
- Error recovery mechanisms

#### 4. ResponseFormatValidation (3 tests)
Tests response format correctness:
```java
@Nested
class ResponseFormatValidation {
    @Test void testUptimeResponseFormat()
    @Test void testWanStatusJsonStructure()
    @Test void testOnlineClientsJsonArray()
}
```

**What's Tested:**
- Semicolon-delimited response structure
- JSON object structure
- JSON array structure

#### 5. PerformanceCharacteristics (2 tests)
Tests performance and concurrency:
```java
@Nested
class PerformanceCharacteristics {
    @Test void testResponseTime()
    @Test void testConcurrentRequests()
}
```

**What's Tested:**
- Response time within timeout
- Concurrent request handling

## Running HTTP Tests

### Run All HTTP Tests
```powershell
cd asus-router-mcp-server
.\gradlew.bat test --tests "HttpClientIntegrationTest"
```

### Run Specific Test Class
```powershell
# Basic HTTP Operations only
.\gradlew.bat test --tests "HttpClientIntegrationTest$BasicHttpOperations"

# Authentication tests only
.\gradlew.bat test --tests "HttpClientIntegrationTest$AuthenticationTests"

# Error handling tests only
.\gradlew.bat test --tests "HttpClientIntegrationTest$ErrorHandlingTests"
```

### Run All Integration Tests
```powershell
.\gradlew.bat test --tests "*IntegrationTest"
```

### Run Full Build with Tests
```powershell
.\gradlew.bat build
```

### Run Tests Without Build
```powershell
.\gradlew.bat test
```

## Mock Router Server Configuration

### Port Configuration
The mock router server uses **port 8889** to avoid conflicts with other tests (RouterToolsIntegrationTest uses 8888).

**Configuration in Test:**
```java
private static final int MOCK_ROUTER_PORT = 8889;

@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("asus.router.host", () -> "localhost");
    registry.add("asus.router.port", () -> MOCK_ROUTER_PORT);
    registry.add("asus.router.connection-timeout", () -> 3000);
    registry.add("asus.router.read-timeout", () -> 3000);
}
```

### Supported Hooks

The MockRouterServer implements responses for these router API hooks:

| Hook | Format | Example Response |
|------|--------|------------------|
| `uptime` | Semicolon-delimited | `"Thu, 09 Dec 2025 22:30:00 +0100;450123"` |
| `is_alive` | Plain text | `"1"` |
| `memory_usage` | Semicolon-delimited | `"256000;128000;128000"` |
| `cpu_usage` | Semicolon-delimited | `"100;45;100;50"` |
| `netdev` | JSON | `{"eth0":{"tx_bytes":256000000,"rx_bytes":192000000}}` |
| `wan_status` | JSON | `{"wan_ipaddr":"203.0.113.1","wan_proto":"dhcp"}` |
| `onlinelist` | JSON array | `[{"mac":"AA:BB:CC:DD:EE:FF","ip":"192.168.1.100"}]` |
| `get_clientlist` | JSON | `{"AA:BB:CC:DD:EE:FF":{"name":"Device1"}}` |

### Adding Custom Mock Responses

To add a new hook response to MockRouterServer:

1. **Locate the AppGetHandler class** inside MockRouterServer
2. **Add a case in the handler switch:**
```java
case "your_new_hook":
    return "your response here";
```

3. **Document the response format** in this guide

## Test Patterns & Best Practices

### 1. Assertion Pattern
```java
// What you're testing
String response = routerCommandExecutor.executeGetCommand("uptime");

// Assertions follow this order
assertNotNull(response, "Response should not be null");           // Existence
assertTrue(response.length() > 0, "Response should not be empty"); // Format
assertTrue(response.contains(";"), "Should be semicolon-delimited"); // Content
```

### 2. Test Naming
- **What**: Test method names use `test` + noun + verb + expected outcome
- **Example**: `testGetRequestWithHook()`, `testAutoAuthentication()`
- **Why**: Makes test purpose immediately clear

### 3. Test Organization with @Nested
```java
@Nested
@DisplayName("Authentication")
class AuthenticationTests {
    @Test void testAutoAuthentication() { }
}
```

**Benefits:**
- Logical grouping of related tests
- Shared setup/teardown between nested class tests
- Improved IDE test tree navigation
- Better test report organization

### 4. Port Management
```java
// Each integration test class uses a different port to avoid conflicts
// RouterToolsIntegrationTest    → port 8888
// HttpClientIntegrationTest     → port 8889
// Custom tests                   → port 8890+
```

### 5. Dynamic Property Configuration
```java
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("asus.router.host", () -> "localhost");
    registry.add("asus.router.port", () -> MOCK_ROUTER_PORT);
    // Spring automatically injects these before test runs
}
```

## Debugging HTTP Tests

### Enable Debug Logging
Set logging level in test `application.yml`:
```yaml
logging:
  level:
    com.asusrouter.infrastructure.adapter.out.http: DEBUG
    org.springframework.web.reactive.function.client: DEBUG
```

### Common Issues

#### 1. Port Already in Use
**Error:** `Address already in use: bind`
**Solution:** Change mock router port or wait for previous test to complete

#### 2. Authentication Failure
**Error:** `RouterAuthenticationException: Authentication failed`
**Solution:** Verify credentials in `@DynamicPropertySource` match mock server setup

#### 3. Timeout Errors
**Error:** `RouterCommunicationException: timeout`
**Solution:** Increase timeout in properties or check if mock server is running

#### 4. JSON Parsing Errors
**Error:** `JsonProcessingException`
**Solution:** Verify mock server returns valid JSON for the hook

## Integration Test Workflow

### Complete Test Execution Flow

```
1. @SpringBootTest annotation
   ↓ Starts Spring Boot application context
2. @DynamicPropertySource
   ↓ Injects test-specific properties
3. @BeforeAll startMockRouter()
   ↓ Starts MockRouterServer on port 8889
4. @Autowired RouterCommandExecutor
   ↓ Injects executor (configured with mock server)
5. Test methods execute
   ↓ RouterCommandExecutor.executeGetCommand()
   ↓ AsusRouterAuthenticator.authenticate()
   ↓ HTTP GET to localhost:8889
   ↓ MockRouterServer responses
6. Assertions validate response
7. @AfterAll stopMockRouter()
   ↓ Shuts down MockRouterServer
8. Context destroyed
```

## Adding New HTTP Tests

### Template for New Test Class

```java
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MyHttpTest {
    
    private static final int MOCK_ROUTER_PORT = 8890; // Use new port
    
    private MockRouterServer mockRouter;
    
    @Autowired
    private RouterCommandExecutor routerCommandExecutor;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("asus.router.host", () -> "localhost");
        registry.add("asus.router.port", () -> MOCK_ROUTER_PORT);
    }
    
    @BeforeAll
    void setup() throws IOException {
        mockRouter = new MockRouterServer(MOCK_ROUTER_PORT, "admin", "test123");
        mockRouter.start();
    }
    
    @AfterAll
    void teardown() {
        if (mockRouter != null) mockRouter.stop();
    }
    
    @Nested
    class MyTests {
        @Test
        void testSomething() {
            // Given
            // When
            // Then
        }
    }
}
```

### Template for New Test Method

```java
@Test
@Order(1)
@DisplayName("Should do something specific")
void testSpecificBehavior() {
    // Given: Setup preconditions
    String hook = "uptime";
    
    // When: Execute the action being tested
    String response = routerCommandExecutor.executeGetCommand(hook);
    
    // Then: Assert expected outcomes
    assertNotNull(response, "Response should not be null");
    assertTrue(response.contains(";"), "Should be semicolon-delimited");
}
```

## Test Metrics

### Current HTTP Test Coverage
- **Test Class:** HttpClientIntegrationTest
- **Total Tests:** 16
- **Test Groups:** 5 nested classes
- **Coverage Areas:**
  - ✅ Basic HTTP operations (5 tests)
  - ✅ Authentication flow (3 tests)
  - ✅ Error handling (3 tests)
  - ✅ Response validation (3 tests)
  - ✅ Performance (2 tests)

### Expected Test Results
When running `HttpClientIntegrationTest`, all tests should **PASS**:
```
BasicHttpOperations:              5 passed
AuthenticationTests:              3 passed
ErrorHandlingTests:               3 passed
ResponseFormatValidation:         3 passed
PerformanceCharacteristics:       2 passed
────────────────────────────────────────
Total:                           16 passed
```

## Troubleshooting Test Failures

### "Failed to start mock router"
**Cause:** Port already in use or permission denied
**Solution:** 
- Check if another test is running on same port
- Change `MOCK_ROUTER_PORT` in test class
- Run tests sequentially instead of in parallel

### "RouterCommandExecutor is null"
**Cause:** Spring context not properly initialized
**Solution:**
- Ensure `@SpringBootTest` annotation is present
- Verify `@Autowired` is used correctly
- Check application context configuration

### "Authentication failed"
**Cause:** Mock server credentials don't match application properties
**Solution:**
```java
// In @DynamicPropertySource
registry.add("asus.router.username", () -> MOCK_USERNAME); // Match MockRouterServer
registry.add("asus.router.password", () -> MOCK_PASSWORD);
```

### "Response parsing failed"
**Cause:** Mock server response format incorrect
**Solution:**
- Verify MockRouterServer response matches hook specification
- Update test assertion expectations if format changed
- Add debug logging to see actual response

## Related Documentation

- **MCP Protocol:** `docs/MCP_PROTOCOL_USAGE.md`
- **Architecture:** `.github/copilot-instructions.md` (Architecture section)
- **Full Test Results:** `docs/TEST_RESULTS.md`
- **Build Status:** `STATUS.md`

## Quick Reference

### Key Classes
- `RouterCommandExecutor` - HTTP client (tests focus here)
- `AsusRouterAuthenticator` - Authentication handler
- `MockRouterServer` - Test infrastructure
- `HttpClientIntegrationTest` - Comprehensive HTTP tests

### Key Ports
- 8888 - RouterToolsIntegrationTest
- 8889 - HttpClientIntegrationTest
- 8890+ - Custom test classes

### Key Files
- `src/test/java/.../HttpClientIntegrationTest.java` - NEW HTTP tests
- `src/test/java/.../MockRouterServer.java` - Mock server
- `src/main/java/.../RouterCommandExecutor.java` - Component under test

