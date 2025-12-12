# ASUS Router MCP Server

A Java 21 Spring Boot application that provides a Model Context Protocol (MCP) server interface for monitoring and managing ASUS routers. This is a direct translation of the Python `RouterInfo` library with all 17 methods implemented as MCP tools.

## 🏗️ Architecture

This project implements **Hexagonal Architecture** (Ports and Adapters) with maximum granularity:

```
┌─────────────────────────────────────────────────────────────┐
│                     Infrastructure Layer                      │
│  ┌──────────────────┐              ┌──────────────────┐    │
│  │   MCP Adapter    │              │  HTTP Adapter    │    │
│  │  (JSON-RPC/stdio)│              │ (Router Client)  │    │
│  └────────┬─────────┘              └────────┬─────────┘    │
├───────────┼──────────────────────────────────┼──────────────┤
│           │      Application Layer           │              │
│  ┌────────▼─────────┐              ┌────────▼─────────┐    │
│  │  Inbound Ports   │              │  Outbound Ports  │    │
│  │  (17 Use Cases)  │              │  (8 Commands)    │    │
│  └────────┬─────────┘              └────────▲─────────┘    │
│           │                                 │              │
│  ┌────────▼─────────────────────────────────┘              │
│  │              Use Case Services                          │
│  │         (Business Logic Layer)                          │
│  └────────┬────────────────────────────────────────────┘    │
├───────────┼──────────────────────────────────────────────────┤
│           │          Domain Layer                           │
│  ┌────────▼─────────────────────────────────────────────┐   │
│  │  Domain Models  │  Value Objects  │  Exceptions      │   │
│  │  (Pure Java)    │  (Validated)    │  (Error Codes)  │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Key Design Principles

1. **Maximum Granularity**: Each router operation has its own port interface
2. **Domain Purity**: Domain layer has zero framework dependencies
3. **Compile-Time Safety**: Annotation processor generates MCP tool schemas at compile time
4. **Reactive Communication**: Spring WebFlux for non-blocking router HTTP calls
5. **Type Safety**: Value objects (IpAddress, MacAddress) with validation

## 🚀 Features

### 17 MCP Tools (All Python RouterInfo Methods)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `asus_router_get_uptime` | Router uptime since last boot | None |
| `asus_router_get_memory_usage` | Memory statistics (total/free/used) | None |
| `asus_router_get_cpu_usage` | CPU usage for all cores | None |
| `asus_router_get_traffic_total` | Total traffic since boot (Mb) | None |
| `asus_router_get_traffic` | Traffic with current speed | None |
| `asus_router_get_wan_status` | WAN connection status | None |
| `asus_router_get_client_full_info` | Complete client info | `mac` |
| `asus_router_get_client_info_summary` | Client summary | `mac` |
| `asus_router_get_online_clients` | List of online clients | None |
| `asus_router_get_dhcp_leases` | DHCP lease table | None |
| `asus_router_get_settings` | Router NVRAM settings | None |
| `asus_router_get_nvram` | Custom NVRAM command | `nvram_command` |
| `asus_router_get_client_list` | Client list with format | `format` (0/1/2) |
| `asus_router_get_network_device_list` | Network devices | `device_name` |
| `asus_router_get_wan_link` | WAN link information | `unit` (0/1) |
| `asus_router_is_alive` | Check router connectivity | None |
| `asus_router_show_info` | Formatted router info | `detailed` |

## 📋 Prerequisites

- **Java 21** (LTS) - Required for record types and modern Java features
- **Gradle 8.x** - Build system with annotation processing
- **ASUS Router** - Running firmware with HTTP API enabled
- **Router Credentials** - Admin username and password

## 🔧 Configuration

Edit `src/main/resources/application.yml`:

```yaml
asus:
  router:
    host: "192.168.1.1"          # Router IP address
    port: 80                      # HTTP port (or 443 for HTTPS)
    use-https: false              # Set true for HTTPS
    username: "admin"             # Router admin username
    password: "your_password"     # Router admin password
    connection-timeout: 5000      # Connection timeout (ms)
    read-timeout: 10000           # Read timeout (ms)

logging:
  level:
    com.asusrouter: DEBUG         # Set to INFO in production
```

**Security Note**: Never commit `application.yml` with real credentials! Use environment variables:

```yaml
asus:
  router:
    password: ${ASUS_ROUTER_PASSWORD}  # Set via environment variable
```

## 🏃 Building and Running

### Build Project

```bash
cd asus-router-mcp-server
./gradlew clean build
```

This will:
1. Compile Java sources
2. Run annotation processor to generate MCP tool schemas
3. Generate `mcp-tools.json` with all tool definitions
4. Run all tests
5. Package executable JAR

### Run as MCP Server

MCP servers communicate via stdio (standard input/output):

```bash
java -jar build/libs/asus-router-mcp-server-1.0.0.jar
```

The server will:
- Read JSON-RPC 2.0 requests from stdin
- Execute MCP tool calls
- Write JSON-RPC 2.0 responses to stdout

### Run as CLI (ShowRouterInfo)

```bash
java -cp build/libs/asus-router-mcp-server-1.0.0.jar \
  com.asusrouter.cli.ShowRouterInfoRunner [--detailed]
```

Options:
- `--detailed`: Show extended information with all clients

## 📦 Project Structure

```
asus-router-mcp-server/
├── src/main/java/com/asusrouter/
│   ├── AsusRouterMcpServerApplication.java          # Spring Boot main
│   ├── domain/
│   │   ├── model/                                   # 11 domain models
│   │   │   ├── Uptime.java
│   │   │   ├── MemoryUsage.java
│   │   │   ├── CpuUsage.java
│   │   │   ├── TrafficTotal.java
│   │   │   ├── TrafficSpeed.java
│   │   │   ├── TrafficWithSpeed.java
│   │   │   ├── WanStatus.java
│   │   │   ├── ClientFullInfo.java
│   │   │   ├── ClientSummary.java
│   │   │   ├── RouterSettings.java
│   │   │   ├── DhcpLease.java
│   │   │   ├── OnlineClient.java
│   │   │   ├── IpAddress.java                       # Value object
│   │   │   ├── MacAddress.java                      # Value object
│   │   │   └── Netmask.java                         # Value object
│   │   └── exception/                               # 5 exception classes
│   │       ├── ErrorCode.java
│   │       ├── RouterException.java
│   │       ├── RouterAuthenticationException.java
│   │       ├── RouterCommunicationException.java
│   │       └── ClientNotFoundException.java
│   ├── application/
│   │   ├── port/
│   │   │   ├── in/                                  # 17 inbound ports
│   │   │   │   ├── GetUptimeUseCase.java
│   │   │   │   ├── GetMemoryUsageUseCase.java
│   │   │   │   └── ... (15 more)
│   │   │   └── out/                                 # 8 outbound ports
│   │   │       ├── RouterUptimePort.java
│   │   │       ├── RouterMemoryPort.java
│   │   │       └── ... (6 more)
│   │   └── service/                                 # 17 use case implementations
│   │       ├── GetUptimeService.java
│   │       ├── GetMemoryUsageService.java
│   │       └── ... (15 more)
│   ├── infrastructure/
│   │   ├── config/
│   │   │   ├── RouterProperties.java
│   │   │   ├── WebClientConfig.java
│   │   │   └── JacksonConfig.java
│   │   └── adapter/
│   │       ├── in/mcp/                              # MCP JSON-RPC adapter
│   │       │   ├── McpJsonRpcHandler.java
│   │       │   └── StdioTransport.java
│   │       └── out/http/                            # HTTP router adapter
│   │           ├── AsusRouterAuthenticator.java
│   │           ├── RouterCommandExecutor.java
│   │           └── adapter/                         # 8 adapter implementations
│   │               ├── HttpRouterUptimeAdapter.java
│   │               └── ... (7 more)
│   └── mcp/
│       ├── annotations/                             # Annotation framework
│       │   ├── McpTool.java
│       │   ├── McpSchema.java
│       │   └── McpParameter.java
│       └── processor/
│           └── McpAnnotationProcessor.java          # Compile-time processor
├── src/main/resources/
│   ├── application.yml
│   └── META-INF/services/
│       └── javax.annotation.processing.Processor
├── src/test/java/                                   # Comprehensive tests
├── build.gradle
├── settings.gradle
├── PROJECT_SPECIFICATION.md                         # 3000+ line spec document
└── README.md
```

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test

```bash
./gradlew test --tests GetUptimeServiceTest
```

### Test Coverage

```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

### Architecture Tests

ArchUnit tests enforce hexagonal architecture rules:
- Domain layer has no framework dependencies
- Application layer depends only on domain
- Infrastructure layer can depend on all layers
- Ports are interfaces, adapters are implementations

## 🔍 MCP Tool Schema Generation

The annotation processor automatically generates:

1. **Individual Tool Schemas**: `*ToolSchema.java` classes
2. **Tool Registry**: `McpToolRegistry.java` with all tools
3. **MCP Tools JSON**: `mcp-tools.json` for client discovery

Generated during compilation:

```bash
./gradlew compileJava
ls build/generated/sources/annotationProcessor/java/main/com/asusrouter/infrastructure/adapters/in/mcp/generated/
```

## 📖 Usage Examples

### As MCP Server (AI Assistant Integration)

Configure in your AI assistant's MCP settings:

```json
{
  "mcpServers": {
    "asus-router": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/asus-router-mcp-server-1.0.0.jar"
      ],
      "env": {
        "ASUS_ROUTER_PASSWORD": "your_password"
      }
    }
  }
}
```

Example queries to AI:
- "Check if my ASUS router is online"
- "Show me the current WAN IP address and connection status"
- "List all connected clients with their IP addresses"
- "What's my router's memory and CPU usage?"
- "Get detailed information for client with MAC AA:BB:CC:DD:EE:FF"

### As Java Library

```java
@Autowired
private GetUptimeUseCase getUptimeUseCase;

@Autowired
private GetOnlineClientsUseCase getOnlineClientsUseCase;

public void monitorRouter() {
    // Get uptime
    Uptime uptime = getUptimeUseCase.execute();
    System.out.println("Router up since: " + uptime.since());
    System.out.println("Uptime seconds: " + uptime.getUptimeSeconds());
    
    // Get online clients
    List<OnlineClient> clients = getOnlineClientsUseCase.execute();
    System.out.println("Online clients: " + clients.size());
    clients.forEach(client -> 
        System.out.printf("  %s - %s%n", 
            client.mac().normalized(), 
            client.ip().value())
    );
}
```

## 🛠️ Development

### Add New MCP Tool

1. **Create Domain Model** (if needed):
```java
@McpSchema(example = """
{
  "field1": "value1",
  "field2": 123
}
""")
public record MyModel(String field1, int field2) {}
```

2. **Create Inbound Port**:
```java
@McpTool(
    name = "asus_router_my_tool",
    description = "Description of what this tool does",
    errorCodes = {ErrorCode.ROUTER_AUTH_FAILED}
)
public interface MyToolUseCase {
    MyModel execute(@McpParameter(name = "param", required = true) String param);
}
```

3. **Implement Use Case**:
```java
@Service
@RequiredArgsConstructor
public class MyToolService implements MyToolUseCase {
    private final RouterSomePort routerPort;
    
    @Override
    public MyModel execute(String param) {
        String raw = routerPort.someMethod(param);
        return parseResponse(raw);
    }
}
```

4. **Rebuild**: Annotation processor generates schema automatically

### Run in Development Mode

```bash
./gradlew bootRun
```

With custom properties:

```bash
./gradlew bootRun --args='--asus.router.host=192.168.50.1 --asus.router.password=mypass'
```

## 📝 Python Equivalence

This Java implementation provides 1:1 equivalence with the Python library:

| Python Method | Java Use Case | MCP Tool |
|---------------|---------------|----------|
| `get_uptime()` | `GetUptimeUseCase` | `asus_router_get_uptime` |
| `get_memory_usage()` | `GetMemoryUsageUseCase` | `asus_router_get_memory_usage` |
| `get_cpu_usage()` | `GetCpuUsageUseCase` | `asus_router_get_cpu_usage` |
| `get_traffic_total()` | `GetTrafficTotalUseCase` | `asus_router_get_traffic_total` |
| `get_traffic()` | `GetTrafficUseCase` | `asus_router_get_traffic` |
| `get_wan_status()` | `GetWanStatusUseCase` | `asus_router_get_wan_status` |
| `get_client_full_info(mac)` | `GetClientFullInfoUseCase` | `asus_router_get_client_full_info` |
| `get_client_info_summary(mac)` | `GetClientInfoSummaryUseCase` | `asus_router_get_client_info_summary` |
| `get_online_clients()` | `GetOnlineClientsUseCase` | `asus_router_get_online_clients` |
| `get_dhcp_leases()` | `GetDhcpLeasesUseCase` | `asus_router_get_dhcp_leases` |
| `get_settings()` | `GetSettingsUseCase` | `asus_router_get_settings` |
| `get_nvram(cmd)` | `GetNvramUseCase` | `asus_router_get_nvram` |
| `get_client_list(fmt)` | `GetClientListUseCase` | `asus_router_get_client_list` |
| `get_network_device_list(dev)` | `GetNetworkDeviceListUseCase` | `asus_router_get_network_device_list` |
| `get_wan_link(unit)` | `GetWanLinkUseCase` | `asus_router_get_wan_link` |
| `is_alive()` | `IsAliveUseCase` | `asus_router_is_alive` |
| `ShowRouterInfo.py` | `ShowRouterInfoUseCase` | `asus_router_show_info` |

## 🤝 Contributing

This is a complete translation project. Contributions welcome for:
- Additional router models support
- Enhanced error handling
- Performance optimizations
- Extended test coverage

## 📄 License

Same license as original Python library.

## 🙏 Acknowledgments

- Original Python `RouterInfo` library authors
- Model Context Protocol (MCP) specification
- ASUS router community

## 📞 Support

For issues with:
- **Java implementation**: Open issue in this repo
- **Router connectivity**: Check ASUS router firmware and network settings
- **MCP protocol**: Refer to MCP specification

---

**Built with ❤️ using Java 21, Spring Boot 3.2.1, and Hexagonal Architecture**
