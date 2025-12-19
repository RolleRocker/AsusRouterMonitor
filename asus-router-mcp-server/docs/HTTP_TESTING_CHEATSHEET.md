# HTTP Testing Cheat Sheet

## Run Commands

```powershell
# 🟢 Run all HTTP tests
.\gradlew.bat test --tests "HttpClientIntegrationTest"

# 🔵 Run specific test group
.\gradlew.bat test --tests "HttpClientIntegrationTest$BasicHttpOperations"
.\gradlew.bat test --tests "HttpClientIntegrationTest$AuthenticationTests"
.\gradlew.bat test --tests "HttpClientIntegrationTest$ErrorHandlingTests"
.\gradlew.bat test --tests "HttpClientIntegrationTest$ResponseFormatValidation"
.\gradlew.bat test --tests "HttpClientIntegrationTest$PerformanceCharacteristics"

# 🟡 Run all integration tests
.\gradlew.bat test --tests "*IntegrationTest"

# ⚫ Full build with all tests
.\gradlew.bat build

# ⭕ Quick build (skip tests)
.\gradlew.bat build -x test

# 📊 View test report
start build\reports\tests\test\index.html
```

## Test File Structure

```
HttpClientIntegrationTest.java (16 tests total)
├── BasicHttpOperations
│   ├── test01: GET with hook
│   ├── test02: GET with hook + parameter
│   ├── test03: Memory usage (semicolon format)
│   ├── test04: CPU usage (semicolon format)
│   └── test05: JSON response
├── AuthenticationTests
│   ├── test01: Auto-login
│   ├── test02: Cookie reuse
│   └── test03: Auth header
├── ErrorHandlingTests
│   ├── test01: Invalid hook
│   ├── test02: Empty response
│   └── test03: Error recovery
├── ResponseFormatValidation
│   ├── test01: Uptime format
│   ├── test02: WAN status JSON
│   └── test03: Online clients array
└── PerformanceCharacteristics
    ├── test01: Response time
    └── test02: Concurrent requests
```

## Test Template

```java
@Nested
@DisplayName("Your Test Category")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class YourTests {
    
    @Test
    @Order(1)
    @DisplayName("Should do X when Y")
    void testSpecificBehavior() {
        // Given: Setup
        String hook = "uptime";
        
        // When: Execute
        String response = routerCommandExecutor.executeGetCommand(hook);
        
        // Then: Assert
        assertNotNull(response, "Response should not be null");
    }
}
```

## Key Test Assertions

```java
// Existence checks
assertNotNull(response, "Response should not be null");
assertNotEquals("", response, "Response should not be empty");

// Format checks
assertTrue(response.contains(";"), "Should be semicolon-delimited");
assertTrue(response.startsWith("{"), "Should be JSON object");
assertTrue(response.startsWith("["), "Should be JSON array");

// Content checks
String[] parts = response.split(";");
assertEquals(3, parts.length, "Should have 3 parts");
assertTrue(parts[0].length() > 0, "First part should not be empty");

// Exception checks
assertThrows(RouterCommunicationException.class, () -> {
    routerCommandExecutor.executeGetCommand(invalidHook);
});
```

## Mock Router Ports

| Port | Test Class |
|------|-----------|
| 8888 | RouterToolsIntegrationTest |
| 8889 | HttpClientIntegrationTest |
| 8890+ | Custom tests |

## Mock Router Hooks

| Hook | Format | Example |
|------|--------|---------|
| `uptime` | `datetime;seconds` | `"Thu, 09 Dec 2025 22:30:00;450123"` |
| `is_alive` | Plain text | `"1"` |
| `memory_usage` | `total;free;used` | `"256000;128000;128000"` |
| `cpu_usage` | `cpu1T;cpu1U;cpu2T;cpu2U` | `"100;45;100;50"` |
| `netdev` | JSON object | `{"eth0":{"tx_bytes":256000000}}` |
| `wan_status` | JSON object | `{"wan_ipaddr":"203.0.113.1"}` |
| `onlinelist` | JSON array | `[{"mac":"AA:BB:CC:DD:EE:FF"}]` |

## Configuration

```java
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("asus.router.host", () -> "localhost");
    registry.add("asus.router.port", () -> 8889);
    registry.add("asus.router.username", () -> "admin");
    registry.add("asus.router.password", () -> "test123");
    registry.add("asus.router.connection-timeout", () -> 3000);
    registry.add("asus.router.read-timeout", () -> 3000);
}
```

## File Locations

| File | Location |
|------|----------|
| HTTP Tests | `src/test/java/com/asusrouter/integration/HttpClientIntegrationTest.java` |
| Full Guide | `docs/HTTP_TESTING_GUIDE.md` |
| Quick Start | `docs/HTTP_TESTING_QUICKSTART.md` |
| This Cheat | `docs/HTTP_TESTING_CHEATSHEET.md` |
| Mock Server | `src/test/java/com/asusrouter/integration/MockRouterServer.java` |
| Component | `src/main/java/com/asusrouter/infrastructure/adapter/out/http/RouterCommandExecutor.java` |

## Test Lifecycle

```
@BeforeAll
  ↓ Create MockRouterServer on port 8889
  ↓ Server starts listening

@Test (per test method)
  ↓ Create test instance
  ↓ Inject RouterCommandExecutor
  ↓ Run test code
  ↓ Assert results

@AfterAll
  ↓ Stop MockRouterServer
  ↓ Clean up resources
```

## Common Issues & Fixes

| Issue | Fix |
|-------|-----|
| Port 8889 in use | Change port in test class or kill process |
| Tests timeout | Increase timeout in @DynamicPropertySource |
| Auth fails | Verify username/password match MockRouterServer |
| JSON parsing error | Check mock server response format |
| Tests hang | Check if MockRouterServer started |

## Adding New Test

1. Find a nested test class or create new one
2. Add `@Test` method with `@Order(n)` annotation
3. Add `@DisplayName("description")`
4. Follow Given-When-Then pattern
5. Add assertions
6. Run: `.\gradlew.bat test --tests "HttpClientIntegrationTest$YourClass"`

## Debugging

```powershell
# Verbose output
.\gradlew.bat test --tests "HttpClientIntegrationTest" -i

# With logging
# Set in src/test/resources/application.yml:
# logging:
#   level:
#     com.asusrouter.infrastructure.adapter.out.http: DEBUG

# Run specific test
.\gradlew.bat test --tests "HttpClientIntegrationTest.BasicHttpOperations.testGetRequestWithHook"
```

## Documentation

- 📖 **Full Guide:** `docs/HTTP_TESTING_GUIDE.md` - Complete reference
- ⚡ **Quick Start:** `docs/HTTP_TESTING_QUICKSTART.md` - Get started in 30s
- 📝 **This Cheat:** `docs/HTTP_TESTING_CHEATSHEET.md` - Quick lookup

## Test Statistics

- **Total Tests:** 16
- **Test Groups:** 5
- **Test Scenarios:** 23+
- **Mock Hooks:** 8+
- **Code Coverage:** Router HTTP layer
- **Pass Rate:** 100% (expected)

## Key Classes

```
RouterCommandExecutor
  ├─ executeGetCommand(String hook)
  └─ executeGetCommand(String hook, String parameter)

AsusRouterAuthenticator
  ├─ authenticate()
  └─ getAuthorizationHeader()

MockRouterServer
  ├─ start()
  ├─ stop()
  ├─ LoginHandler (simulates /login.cgi)
  └─ AppGetHandler (simulates /appGet.cgi)

HttpClientIntegrationTest
  ├─ BasicHttpOperations (5 tests)
  ├─ AuthenticationTests (3 tests)
  ├─ ErrorHandlingTests (3 tests)
  ├─ ResponseFormatValidation (3 tests)
  └─ PerformanceCharacteristics (2 tests)
```

## One-Liners

```powershell
# Run and show report
.\gradlew.bat test --tests "HttpClientIntegrationTest" && start build\reports\tests\test\index.html

# Run specific category
.\gradlew.bat test --tests "HttpClientIntegrationTest$BasicHttpOperations"

# Clean and test
.\gradlew.bat clean build

# Fast test (no full build)
.\gradlew.bat test --tests "HttpClientIntegrationTest" -x compileJava
```

---

**Last Updated:** 2025-12-18  
**Version:** 1.0  
**Status:** Ready for Production

