# ASUS Router MCP Server - AI Coding Instructions (IntelliJ Version)

## Project Overview

Java 21 Spring Boot application implementing Model Context Protocol (MCP) server for ASUS router monitoring. Direct translation of Python `RouterInfo` library with 17 monitoring methods exposed as MCP tools. Uses **Hexagonal Architecture** (Ports & Adapters) with maximum granularity.

## Architecture (Critical Understanding)

### Three-Layer Hexagonal Structure

```
Domain Layer (Pure Java) ← Application Layer (Use Cases) ← Infrastructure Layer (Adapters)
```

**Domain Layer** (`com.asusrouter.domain`):
- Pure Java records with NO Spring/framework dependencies
- Models: `Uptime`, `MemoryUsage`, `CpuUsage`, `ClientFullInfo`, etc. (17 total)
- Value objects: `MacAddress`, `IpAddress`, `Hostname` (with validation in constructors)
- Exceptions: `RouterException` with `ErrorCode` enum
- **Rule**: Domain NEVER imports from `application` or `infrastructure`

**Application Layer** (`com.asusrouter.application`):
- **Port interfaces** (`port.in`): 17 use cases annotated with `@McpTool`, defining MCP tool signatures
- **Port interfaces** (`port.out`): 8 outbound ports for HTTP operations (e.g., `RouterUptimePort`)
- **Service implementations** (`service`): Business logic implementing inbound ports
- Services inject outbound ports (dependency inversion)

**Infrastructure Layer** (`com.asusrouter.infrastructure`):
- **Inbound adapters** (`adapter.in.mcp`): MCP JSON-RPC protocol handlers
  - `McpJsonRpcHandler`: Routes 17 tools to use cases
  - `McpStdioTransport`: stdin/stdout MCP communication
- **Outbound adapters** (`adapter.out.http.adapter`): 8 HTTP adapters implementing outbound ports
  - `HttpRouterUptimeAdapter`, `HttpRouterMemoryAdapter`, etc.
  - Use `RouterCommandExecutor` for authenticated HTTP calls
- **CLI adapter** (`cli.ShowRouterInfoRunner`): Command-line interface

### Key Pattern: Maximum Granularity

Each router operation has dedicated port interfaces:
- `GetUptimeUseCase` (in) → `RouterUptimePort` (out) → `HttpRouterUptimeAdapter`
- One method per interface for ultimate flexibility and testability

## Development Workflows (IntelliJ)

### Build Commands (Gradle Tool Window)

In IntelliJ's Gradle tool window (right sidebar):
- **build** → Full build with tests
- **build -x test** → Build without tests (faster)
- **test** → Run tests only
- **clean** → Clean build outputs

Or use terminal in IntelliJ:
```cmd
.\gradlew.bat build          # Full build with tests
.\gradlew.bat build -x test  # Build without tests (faster)
.\gradlew.bat test           # Run tests only
.\gradlew.bat clean build    # Clean rebuild
```

### Running the Application in IntelliJ

**As MCP Server** (stdio mode):
1. Right-click `AsusRouterMcpServerApplication.java` → Run
2. Or use Gradle: `bootRun` task

**As CLI**:
1. Go to Run → Edit Configurations
2. Create new Application configuration
3. Main class: `com.asusrouter.AsusRouterMcpServerApplication`
4. Program arguments: `--cli` or `--cli --detailed`
5. Run the configuration

### Configuration

Router credentials in `src/main/resources/application.yml`:
```yaml
asus:
  router:
    host: "192.168.1.1"
    username: "admin"
    password: "${ASUS_ROUTER_PASSWORD}"  # Use env var for security
    connection-timeout: 5000
    read-timeout: 10000
```

**Setting environment variables in IntelliJ:**
1. Run → Edit Configurations
2. Select your run configuration
3. Add environment variables: `ASUS_ROUTER_PASSWORD=your_password`

## Project-Specific Conventions

### 1. Domain Models as Records

All domain models are **Java records** with validation in compact constructors:

```java
public record Uptime(String since, String uptime) {
    public Uptime {
        if (since == null || uptime == null) {
            throw new IllegalArgumentException("Uptime fields cannot be null");
        }
    }
}
```

**IntelliJ tip:** Use Alt+Insert → Record to generate records quickly

### 2. MCP Tool Annotation Pattern

Inbound ports use `@McpTool` for tool registration (SOURCE retention):

```java
@McpTool(
    name = "asus_router_get_client_info_summary",
    description = "Retrieve summary information about a specific connected client",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR", "CLIENT_NOT_FOUND"}
)
public interface GetClientInfoSummaryUseCase {
    ClientSummary execute(@McpParameter(name = "mac", required = true) MacAddress mac);
}
```

**IntelliJ tip:** Ctrl+Click on annotation to navigate to definition

### 3. Response Parsing in Services

Router HTTP responses use MIXED formats - parse appropriately in service layer:

**Simple data (semicolon-delimited):**
```java
// Uptime: "Thu, 09 Dec 2025 22:30:00 +0100;450123"
private Uptime parseUptimeResponse(String response) {
    String[] parts = response.split(";");
    if (parts.length != 2) {
        throw new IllegalStateException("Invalid uptime response format: " + response);
    }
    return new Uptime(parts[0].trim(), parts[1].trim());
}
```

**Complex data (JSON):**
```java
// Traffic: {"eth0":{"tx_bytes":256000000,"rx_bytes":192000000}}
private TrafficTotal parseTrafficResponse(String response) {
    JsonNode root = objectMapper.readTree(response);
    JsonNode wanInterface = findWanInterface(root);
    long sentBytes = wanInterface.path("tx_bytes").asLong();
    // ... convert to domain model
}
```

### 4. HTTP Adapter Pattern

All HTTP adapters follow this template with specific hook names:

```java
@Component
@RequiredArgsConstructor
public class HttpRouterUptimeAdapter implements RouterUptimePort {
    private final RouterCommandExecutor commandExecutor;
    private static final String UPTIME_HOOK = "uptime";
    
    @Override
    public String getUptime() {
        return commandExecutor.executeGetCommand(UPTIME_HOOK);
    }
}
```

**IntelliJ tip:** Ctrl+Alt+Shift+T → Refactor menu for safe refactoring

**Adapter Hook Names Reference:**
- `uptime` → semicolon-delimited: "datetime;seconds"
- `memory_usage` → semicolon-delimited: "total;free;used"
- `cpu_usage` → semicolon-delimited: "cpu1Total;cpu1Usage;cpu2Total;cpu2Usage"
- `netdev` → JSON: traffic statistics with tx/rx bytes
- `wan_status` → JSON: WAN connection details
- `onlinelist` → JSON array: online clients [{mac, ip}, ...]
- `dhcp_leases` → JSON array: DHCP leases
- `get_clientlist` → JSON object: client details by MAC
- `nvram_dump` → JSON object: all router settings
- `nvram_get` → plain text: single NVRAM value
- `get_network_device_list` → JSON: network devices
- `get_wan_link` → JSON: WAN link info
- `is_alive` → plain text: "1" or "0"

### 5. Error Handling Convention

Use `RouterException` with `ErrorCode` enum for domain errors:

```java
throw new RouterAuthenticationException(
    "Authentication failed", 
    ErrorCode.ROUTER_AUTH_FAILED
);
```

## Testing Strategy

### ArchUnit Tests

`HexagonalArchitectureTest.java` enforces architecture boundaries:
- Domain layer purity (no Spring dependencies)
- Layered access rules (domain ← application ← infrastructure)
- All domain models must be records
- All port interfaces must be interfaces

**IntelliJ tip:** Run tests with Ctrl+Shift+F10 or right-click → Run

**Current Status**: 5 tests failing (review test expectations, not implementation)

### Integration Tests

`RouterIntegrationTest.java`: End-to-end tests with real router (requires config)
`McpProtocolIntegrationTest.java`: MCP JSON-RPC protocol validation

**IntelliJ tip:** Use Test Runner window (Alt+4) to view test results

## Cross-Component Communication

### MCP Protocol Flow

1. **stdin** → `McpStdioTransport` (reads JSON-RPC requests)
2. → `McpJsonRpcHandler` (routes by method name)
3. → Use case service (e.g., `GetUptimeService`)
4. → Outbound port → HTTP adapter → `RouterCommandExecutor`
5. → ASUS Router HTTP API (`/appGet.cgi?hook=uptime`)
6. ← Response parsed in service → Domain model
7. ← JSON response → **stdout**

**IntelliJ tip:** Use Ctrl+Alt+U to view UML diagram of class relationships

### Router Authentication Flow

`AsusRouterAuthenticator` handles login cookie management:
- Initial authentication via `/login.cgi`
- Cookie stored for subsequent requests
- `RouterCommandExecutor` applies auth to all requests

## Testing Patterns

### MockRouterServer Pattern

Use `MockRouterServer` for integration testing without physical router:

```java
@SpringBootTest
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("asus.router.host", () -> "localhost");
    registry.add("asus.router.port", () -> 8888);
}

@BeforeAll
static void startMockRouter() throws IOException {
    mockServer = HttpServer.create(new InetSocketAddress(8888), 0);
    mockServer.createContext("/login.cgi", new LoginHandler());
    mockServer.createContext("/appGet.cgi", new AppGetHandler());
    mockServer.start();
}

@AfterAll
static void stopMockRouter() {
    if (mockServer != null) mockServer.stop(0);
}
```

**MockRouterServer endpoints**: `/login.cgi` (auth), `/appGet.cgi?hook=uptime` (and 16+ other hooks)

### Integration Test Structure

Three test categories:

1. **RouterToolsIntegrationTest** - Direct tool execution (22 tests):
   - Inject use case interfaces (`@Autowired GetUptimeUseCase`)
   - Call `execute()` methods directly
   - Validate domain model responses

2. **McpProtocolIntegrationTest** - JSON-RPC protocol (19 tests):
   - Send JSON-RPC 2.0 requests to `McpJsonRpcHandler`
   - Validate response structure and content
   - Test error handling

3. **CliRunnerIntegrationTest** - CLI output (12 tests):
   - Execute `ShowRouterInfoUseCase`
   - Validate formatted output (box-drawing, UTF-8)
   - Verify content accuracy

### Test Naming Convention

- Integration tests: `test01_ToolName()` with incremental numbers
- Nested suites: `@Nested class ErrorHandlingTests`
- Unit tests: `shouldReturnXxxWhenYyy()` (BDD style)

## Common Pitfalls

1. **Don't add Spring annotations to domain layer** - Domain models are pure Java records
2. **Parse responses in services, not adapters** - Adapters return raw strings, services parse
3. **Use value objects for validation** - `MacAddress`, `IpAddress` validate in constructors
4. **One method per port interface** - Don't combine multiple operations in one interface
5. **Annotation processor incomplete** - `@McpTool` schema generation not yet functional (runtime registration used instead)
6. **MockRouterServer port 8888** - Hardcoded in tests, don't use for production

## Important Files

- `PROJECT_SPECIFICATION.md`: Complete technical specification (1500+ lines with testing)
- `STATUS.md`: Current build status and completed work
- `build.gradle`: Java 21, Spring Boot 3.4.1, Lombok 1.18.38, ArchUnit 1.3.0
- `application.yml`: Router connection configuration
- `McpJsonRpcHandler.java`: Tool routing switch statement (17 cases)
- `GetUptimeService.java`: Reference service implementation pattern
- `MockRouterServer.java`: HTTP server simulator for integration tests (280 lines)
- `RouterToolsIntegrationTest.java`: 22 integration tests for all 17 tools

## IntelliJ-Specific Features

### Navigation Shortcuts
- **Ctrl+N**: Navigate to class
- **Ctrl+Shift+N**: Navigate to file
- **Ctrl+Alt+Shift+N**: Navigate to symbol
- **Ctrl+B**: Go to declaration/implementation
- **Alt+F7**: Find usages
- **Ctrl+F12**: Show structure of current class

### Code Generation
- **Alt+Insert**: Generate code (constructors, getters, etc.)
- **Ctrl+Alt+L**: Reformat code (uses Project code style)
- **Ctrl+Alt+O**: Optimize imports

### Refactoring
- **Shift+F6**: Rename
- **Ctrl+Alt+M**: Extract method
- **Ctrl+Alt+V**: Extract variable
- **Ctrl+Alt+C**: Extract constant

### Spring Boot Tools
- **Spring Tool Window**: View beans, endpoints, mappings
- **Run Dashboard**: Manage multiple run configurations
- **HTTP Client**: Test endpoints (Tools → HTTP Client)

### Lombok Support
- IntelliJ IDEA comes with Lombok support built-in
- Annotation processing is enabled in `.idea/compiler.xml`
- No additional plugin required (unlike older versions)

## GitHub Copilot in IntelliJ

### Inline Suggestions
- Copilot provides suggestions as you type
- **Tab** to accept suggestion
- **Esc** to dismiss
- **Alt+]** for next suggestion
- **Alt+[** for previous suggestion

### Chat Window
- Open with **Ctrl+Shift+A** → "GitHub Copilot Chat"
- Ask questions about code, architecture, or specific implementations
- Reference files with `@filename` in chat

### Context Awareness
- Copilot reads `.idea/copilot/` and `.github/copilot-instructions.md`
- Understands project structure and hexagonal architecture
- Knows about MCP protocol, router hooks, and testing patterns

### Best Practices
- Reference specific files or classes when asking questions
- Use `@workspace` to query entire project
- Ask for explanations of architecture patterns
- Request test generation with context

## When Adding New Tools

1. Create domain model (record in `domain.model`)
2. Create inbound port interface (`application.port.in`, annotate with `@McpTool`)
3. Create outbound port interface (`application.port.out`)
4. Implement service (`application.service`, inject outbound port)
5. Implement HTTP adapter (`infrastructure.adapter.out.http.adapter`)
6. Add routing case to `McpJsonRpcHandler.dispatch()`
7. Add tool name to `McpJsonRpcHandler.listTools()`

**IntelliJ workflow:**
- Use Alt+Insert for quick class generation
- Use Ctrl+Shift+T to create corresponding test class
- Use Ctrl+Alt+B to navigate to implementations
- Use code templates (Settings → Editor → Live Templates) for common patterns
