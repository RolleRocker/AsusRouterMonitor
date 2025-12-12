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

### Setup Environment Variables

Create a `.env` file in the project root (see `.env.example`):

```bash
# Windows PowerShell
$env:ASUS_ROUTER_HOST="192.168.1.1"
$env:ASUS_ROUTER_USERNAME="admin"
$env:ASUS_ROUTER_PASSWORD="your_password"

# Windows CMD
set ASUS_ROUTER_HOST=192.168.1.1
set ASUS_ROUTER_USERNAME=admin
set ASUS_ROUTER_PASSWORD=your_password

# Linux/Mac
export ASUS_ROUTER_HOST="192.168.1.1"
export ASUS_ROUTER_USERNAME="admin"
export ASUS_ROUTER_PASSWORD="your_password"
```

### Build Project

```bash
cd asus-router-mcp-server

# Windows
.\gradlew.bat clean build

# Linux/Mac
./gradlew clean build
```

This will:
1. Compile Java sources with annotation processing
2. Run 75+ integration tests with MockRouterServer
3. Generate test coverage reports
4. Package executable JAR in `build/libs/`

**Skip tests for faster builds**:
```bash
.\gradlew.bat clean build -x test
```

### Run as MCP Server

MCP servers communicate via stdio (standard input/output):

```bash
# Windows
java -jar build\libs\asus-router-mcp-server-1.0.0-SNAPSHOT.jar

# Linux/Mac
java -jar build/libs/asus-router-mcp-server-1.0.0-SNAPSHOT.jar
```

The server will:
- Read JSON-RPC 2.0 requests from stdin
- Execute MCP tool calls
- Write JSON-RPC 2.0 responses to stdout

**With environment variables**:
```bash
# Windows PowerShell
$env:ASUS_ROUTER_PASSWORD="your_password"
java -jar build\libs\asus-router-mcp-server-1.0.0-SNAPSHOT.jar

# Linux/Mac
ASUS_ROUTER_PASSWORD=your_password java -jar build/libs/asus-router-mcp-server-1.0.0-SNAPSHOT.jar
```

### Run as CLI (ShowRouterInfo)

```bash
# Basic output
java -jar build\libs\asus-router-mcp-server-1.0.0-SNAPSHOT.jar --cli

# Detailed output with all client information
java -jar build\libs\asus-router-mcp-server-1.0.0-SNAPSHOT.jar --cli --detailed
```

**CLI Output Example**:
```
╔═══════════════════════════════════════════╗
║        ASUS Router Information            ║
╠═══════════════════════════════════════════╣
║ Router Status: ✓ Online                   ║
║ Uptime: 5 days, 14 hours                  ║
║ WAN IP: 203.0.113.42                      ║
║ Memory: 45.2% (128 MB / 283 MB)           ║
║ CPU: 12.3%                                ║
║ Online Clients: 8                         ║
╚═══════════════════════════════════════════╝
```

## 📦 Project Structure

```
asus-router-mcp-server/
├── src/main/java/com/asusrouter/
│   ├── AsusRouterMcpServerApplication.java          # Spring Boot main
│   ├── domain/
│   │   ├── model/                                   # 11 domain models (records)
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
│   │   │   └── OnlineClient.java
│   │   ├── value/                                   # Value objects
│   │   │   ├── IpAddress.java
│   │   │   ├── MacAddress.java
│   │   │   ├── Hostname.java
│   │   │   └── Netmask.java
│   │   └── exception/                               # 5 exception classes
│   │       ├── ErrorCode.java
│   │       ├── RouterException.java
│   │       ├── RouterAuthenticationException.java
│   │       ├── RouterCommunicationException.java
│   │       └── ClientNotFoundException.java
│   ├── application/
│   │   ├── port/
│   │   │   ├── in/                                  # 17 inbound ports (use cases)
│   │   │   │   ├── GetUptimeUseCase.java
│   │   │   │   ├── GetMemoryUsageUseCase.java
│   │   │   │   ├── GetCpuUsageUseCase.java
│   │   │   │   ├── GetTrafficTotalUseCase.java
│   │   │   │   ├── GetTrafficUseCase.java
│   │   │   │   ├── GetWanStatusUseCase.java
│   │   │   │   ├── GetClientFullInfoUseCase.java
│   │   │   │   ├── GetClientInfoSummaryUseCase.java
│   │   │   │   ├── GetOnlineClientsUseCase.java
│   │   │   │   ├── GetDhcpLeasesUseCase.java
│   │   │   │   ├── GetSettingsUseCase.java
│   │   │   │   ├── GetNvramUseCase.java
│   │   │   │   ├── GetClientListUseCase.java
│   │   │   │   ├── GetNetworkDeviceListUseCase.java
│   │   │   │   ├── GetWanLinkUseCase.java
│   │   │   │   ├── IsAliveUseCase.java
│   │   │   │   └── ShowRouterInfoUseCase.java
│   │   │   └── out/                                 # 8 outbound ports
│   │   │       ├── RouterUptimePort.java
│   │   │       ├── RouterMemoryPort.java
│   │   │       ├── RouterCpuPort.java
│   │   │       ├── RouterTrafficPort.java
│   │   │       ├── RouterWanPort.java
│   │   │       ├── RouterClientsPort.java
│   │   │       ├── RouterSettingsPort.java
│   │   │       └── RouterNetworkPort.java
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
│   │       │   ├── McpJsonRpcHandler.java           # Routes 17 tools
│   │       │   └── McpStdioTransport.java           # stdin/stdout
│   │       └── out/http/                            # HTTP router adapter
│   │           ├── AsusRouterAuthenticator.java     # Login flow
│   │           ├── RouterCommandExecutor.java       # HTTP client
│   │           └── adapter/                         # 8 HTTP adapters
│   │               ├── HttpRouterUptimeAdapter.java
│   │               ├── HttpRouterMemoryAdapter.java
│   │               └── ... (6 more)
│   ├── cli/
│   │   └── ShowRouterInfoRunner.java               # CLI interface
│   └── mcp/
│       ├── annotations/                             # Annotation framework
│       │   ├── McpTool.java                         # Tool metadata
│       │   ├── McpSchema.java                       # JSON schema
│       │   └── McpParameter.java                    # Parameter metadata
│       └── processor/
│           └── McpAnnotationProcessor.java          # Compile-time processor
├── src/main/resources/
│   ├── application.yml                              # Router configuration
│   └── META-INF/services/
│       └── javax.annotation.processing.Processor
├── src/test/java/com/asusrouter/
│   ├── domain/model/                                # Domain model tests
│   ├── application/service/                         # Service unit tests
│   ├── infrastructure/adapter/                      # Adapter tests
│   ├── integration/                                 # Integration tests
│   │   ├── MockRouterServer.java                    # HTTP server simulator
│   │   ├── RouterToolsIntegrationTest.java          # 22 tool tests
│   │   ├── McpProtocolIntegrationTest.java          # 19 JSON-RPC tests
│   │   ├── CliRunnerIntegrationTest.java            # 12 CLI tests
│   │   ├── McpStdioIntegrationTest.java             # 6 stdio tests
│   │   └── RouterIntegrationTest.java               # Real router tests
│   └── architecture/
│       └── HexagonalArchitectureTest.java           # ArchUnit rules
├── build.gradle                                     # Java 21, Lombok 1.18.38
├── settings.gradle
├── .env.example                                     # Environment variables template
├── .gitignore                                       # Excludes .env
├── PROJECT_SPECIFICATION.md                         # 1374-line specification
├── STATUS.md                                        # Current development status
├── CONTINUE.md                                      # Development guide
└── README.md                                        # This file
```
## 🔍 MCP Protocol

### Tool Discovery

The MCP server exposes all 17 tools via JSON-RPC 2.0. Clients can discover available tools:

**Request** (stdin):
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list"
}
```

**Response** (stdout):
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "tools": [
      {
        "name": "asus_router_get_uptime",
        "description": "Retrieve router uptime information",
        "inputSchema": {
          "type": "object",
          "properties": {},
          "required": []
        }
      },
      {
        "name": "asus_router_get_client_info_summary",
        "description": "Retrieve summary information about a specific connected client",
        "inputSchema": {
          "type": "object",
          "properties": {
            "mac": {
              "type": "string",
              "description": "Client MAC address"
            }
          },
          "required": ["mac"]
        }
      }
      // ... 15 more tools
    ]
  }
}
```

### Tool Invocation

**Request**:
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "asus_router_get_uptime",
    "arguments": {}
  }
}
```

**Response**:
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "{\"since\":\"2024-01-15T10:30:00\",\"uptime\":\"5 days, 14:23:42\"}"
      }
    ]
  }
}
```

### Error Handling

**Response** (error):
```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "error": {
    "code": -32000,
    "message": "ROUTER_AUTH_FAILED: Authentication failed with router",
    "data": {
      "errorCode": "ROUTER_AUTH_FAILED",
      "details": "Invalid username or password"
    }
  }
}
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
