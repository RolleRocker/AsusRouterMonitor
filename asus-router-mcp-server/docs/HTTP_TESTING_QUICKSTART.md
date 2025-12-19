# HTTP Testing Quick Start Guide

## What You Now Have

You have a complete HTTP testing setup with:

1. ✅ **HttpClientIntegrationTest.java** - 16 comprehensive HTTP tests
2. ✅ **HTTP_TESTING_GUIDE.md** - Complete testing documentation
3. ✅ **MockRouterServer** - Already integrated for testing
4. ✅ **Ready-to-run test framework** - All tests pass

## Running Your HTTP Tests (30 seconds)

### Run All HTTP Tests
```powershell
cd asus-router-mcp-server
.\gradlew.bat test --tests "HttpClientIntegrationTest"
```

### Run Specific Test Group
```powershell
# Only basic HTTP operations
.\gradlew.bat test --tests "HttpClientIntegrationTest$BasicHttpOperations"

# Only authentication tests
.\gradlew.bat test --tests "HttpClientIntegrationTest$AuthenticationTests"

# Only error handling
.\gradlew.bat test --tests "HttpClientIntegrationTest$ErrorHandlingTests"
```

## What the Tests Cover

### ✅ Basic HTTP Operations (5 tests)
- GET requests with hooks
- GET requests with parameters
- Semicolon-delimited responses
- JSON responses

### ✅ Authentication (3 tests)
- Automatic login on first request
- Cookie reuse across requests
- Authorization header injection

### ✅ Error Handling (3 tests)
- Invalid hook handling
- Empty responses
- Error recovery

### ✅ Response Validation (3 tests)
- Uptime format validation
- WAN status JSON structure
- Online clients array validation

### ✅ Performance (2 tests)
- Response time within timeout
- Concurrent request handling

## Test Structure

```
HttpClientIntegrationTest
├── BasicHttpOperations (5 tests)
│   ├── testGetRequestWithHook
│   ├── testGetRequestWithHookAndParameter
│   ├── testMemoryUsageHook
│   ├── testCpuUsageHook
│   └── testJsonResponse
├── AuthenticationTests (3 tests)
│   ├── testAutoAuthentication
│   ├── testAuthenticationReuse
│   └── testAuthorizationHeader
├── ErrorHandlingTests (3 tests)
│   ├── testNonExistentHook
│   ├── testEmptyResponse
│   └── testRecoveryFromFailure
├── ResponseFormatValidation (3 tests)
│   ├── testUptimeResponseFormat
│   ├── testWanStatusJsonStructure
│   └── testOnlineClientsJsonArray
└── PerformanceCharacteristics (2 tests)
    ├── testResponseTime
    └── testConcurrentRequests
```

## Key Files

| File | Purpose |
|------|---------|
| `HttpClientIntegrationTest.java` | 16 HTTP client tests (NEW) |
| `HTTP_TESTING_GUIDE.md` | Complete testing documentation (NEW) |
| `RouterCommandExecutor.java` | HTTP client component being tested |
| `AsusRouterAuthenticator.java` | Authentication handler |
| `MockRouterServer.java` | Mock HTTP server for testing |

## How It Works

```
Test Class (HttpClientIntegrationTest)
    ↓ @SpringBootTest - Start Spring context
    ↓ @DynamicPropertySource - Configure test properties
    ↓ @BeforeAll - Start MockRouterServer on port 8889
    ↓ @Autowired RouterCommandExecutor
    ↓ Test methods execute
        → executeGetCommand("uptime")
        → AsusRouterAuthenticator.authenticate()
        → HTTP GET to localhost:8889/appGet.cgi?hook=uptime
        → MockRouterServer responds with simulated data
        → Assertions validate response
    ↓ @AfterAll - Stop MockRouterServer
```

## Adding New HTTP Tests

### 1. Add a new nested test class
```java
@Nested
@DisplayName("New Test Category")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NewTests {
    
    @Test
    @Order(1)
    @DisplayName("Should test something specific")
    void testSomething() {
        // Given
        String hook = "some_hook";
        
        // When
        String response = routerCommandExecutor.executeGetCommand(hook);
        
        // Then
        assertNotNull(response, "Response should not be null");
    }
}
```

### 2. Run your new tests
```powershell
.\gradlew.bat test --tests "HttpClientIntegrationTest$NewTests"
```

## Configuration

### Mock Router Server Details
- **Port:** 8889 (separate from RouterToolsIntegrationTest port 8888)
- **Host:** localhost
- **Username:** admin
- **Password:** test123
- **Timeout:** 3 seconds

### Router API Hooks Supported
- `uptime` - System uptime
- `is_alive` - Router status
- `memory_usage` - Memory statistics
- `cpu_usage` - CPU statistics
- `netdev` - Network traffic
- `wan_status` - WAN connection info
- `onlinelist` - Connected clients
- `get_clientlist` - Detailed client info
- And more...

## Common Tasks

### Debug a Failing Test
1. Add logging to the test method
2. Run with verbose output: `.\gradlew.bat test --tests "HttpClientIntegrationTest$BasicHttpOperations" -i`
3. Check the test report: `build/reports/tests/test/index.html`

### See Test Reports
```powershell
# After running tests, open:
# Windows Explorer: build\reports\tests\test\index.html
# PowerShell:
start build\reports\tests\test\index.html
```

### Run Tests in CI/CD
```yaml
# In your CI/CD pipeline:
gradle test --tests "HttpClientIntegrationTest"
```

## Troubleshooting

### "Port already in use"
```powershell
# Find and stop process on port 8889
netstat -ano | findstr :8889
taskkill /PID <PID> /F
```

### "Tests hang or timeout"
- Increase timeout in `@DynamicPropertySource`
- Check if mock server started properly
- Check firewall settings

### "Authentication failed"
- Verify mock server credentials match test configuration
- Check that username/password are properly set in @DynamicPropertySource

## Related Documentation

- **Full HTTP Testing Guide:** `docs/HTTP_TESTING_GUIDE.md`
- **Architecture Documentation:** `.github/copilot-instructions.md`
- **Test Results:** `docs/TEST_RESULTS.md`
- **Project Status:** `STATUS.md`

## Next Steps

1. ✅ Review the test file: `src/test/java/.../HttpClientIntegrationTest.java`
2. ✅ Read the full guide: `docs/HTTP_TESTING_GUIDE.md`
3. ✅ Run the tests: `.\gradlew.bat test --tests "HttpClientIntegrationTest"`
4. ✅ View test reports: `build/reports/tests/test/index.html`
5. ✅ Add your own HTTP tests following the patterns

## Quick Reference Commands

```powershell
# Run HTTP tests only
.\gradlew.bat test --tests "HttpClientIntegrationTest"

# Run all integration tests
.\gradlew.bat test --tests "*IntegrationTest"

# Run full build with all tests
.\gradlew.bat build

# Run tests and see failures
.\gradlew.bat test --tests "HttpClientIntegrationTest" -i

# Clean and rebuild
.\gradlew.bat clean build
```

## Summary

Your HTTP testing setup is complete and ready to use! 

- **16 comprehensive tests** covering all HTTP scenarios
- **Proper test organization** with nested classes for clarity
- **Mock server infrastructure** for testing without a physical router
- **Complete documentation** for maintenance and extension
- **Best practices** following Spring Boot and JUnit 5 standards

Start by running the tests and reading the full HTTP_TESTING_GUIDE.md for detailed information!

