# MCP Protocol Usage Guide

This guide explains how to interact with the ASUS Router MCP Server using the Model Context Protocol (MCP) over JSON-RPC 2.0.

## Table of Contents
1. [Overview](#overview)
2. [JSON-RPC 2.0 Protocol](#json-rpc-20-protocol)
3. [Available MCP Tools](#available-mcp-tools)
4. [Command Line Usage](#command-line-usage)
5. [Integration with AI Assistants](#integration-with-ai-assistants)
6. [Error Codes](#error-codes)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

## Overview

The MCP Server implements JSON-RPC 2.0 protocol over standard input/output (stdio). This allows AI assistants and other applications to communicate with the ASUS router through a standardized interface.

## JSON-RPC 2.0 Protocol

### Request Format

All requests must follow JSON-RPC 2.0 specification:

```json
{
  "jsonrpc": "2.0",
  "method": "method_name",
  "params": { /* parameters object */ },
  "id": "unique_request_id"
}
```

**Fields:**
- `jsonrpc` (required): Must be "2.0"
- `method` (required): The MCP tool name to invoke
- `params` (optional): Parameters object (use empty `{}` if none)
- `id` (optional): Request identifier. Omit for notifications (no response expected)

### Response Format

**Success Response:**
```json
{
  "jsonrpc": "2.0",
  "result": { /* result object */ },
  "id": "unique_request_id"
}
```

**Error Response:**
```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": -32001,
    "message": "Error description",
    "data": "Additional error details"
  },
  "id": "unique_request_id"
}
```

## Available MCP Tools

### 1. tools/list
List all available MCP tools.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "tools/list",
  "params": {},
  "id": 1
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "tools": [
      "asus_router_get_uptime",
      "asus_router_get_memory_usage",
      ...
    ]
  },
  "id": 1
}
```

### 2. asus_router_get_uptime
Get router uptime and last boot time.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "asus_router_get_uptime",
  "params": {},
  "id": 2
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "uptimeSeconds": 86400,
    "lastBootTime": "2024-12-07T10:00:00Z"
  },
  "id": 2
}
```

### 3. asus_router_is_alive
Check if router is responsive.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "asus_router_is_alive",
  "params": {},
  "id": 3
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "result": true,
  "id": 3
}
```

### 4. asus_router_get_memory_usage
Get memory usage statistics.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "asus_router_get_memory_usage",
  "params": {},
  "id": 4
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "totalMb": 1024,
    "usedMb": 512,
    "freeMb": 512,
    "usagePercent": 50.0
  },
  "id": 4
}
```

### 5. asus_router_get_cpu_usage
Get CPU usage with per-core breakdown.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "asus_router_get_cpu_usage",
  "params": {},
  "id": 5
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "totalUsagePercent": 25.5,
    "coreUsages": [
      {"coreId": 0, "usagePercent": 30.0},
      {"coreId": 1, "usagePercent": 21.0}
    ]
  },
  "id": 5
}
```

### 6. asus_router_get_wan_status
Get WAN connection status.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "asus_router_get_wan_status",
  "params": {},
  "id": 6
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "status": "Connected",
    "ip": {"value": "192.168.1.100"},
    "gateway": {"value": "192.168.1.1"},
    "mask": {"value": "255.255.255.0"},
    "dns": [
      {"value": "8.8.8.8"},
      {"value": "8.8.4.4"}
    ]
  },
  "id": 6
}
```

### 7. asus_router_get_online_clients
Get list of currently connected clients.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "asus_router_get_online_clients",
  "params": {},
  "id": 7
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "count": 2,
    "clients": [
      {
        "mac": {"value": "AA:BB:CC:DD:EE:FF"},
        "name": {"value": "Phone"},
        "ip": {"value": "192.168.1.101"}
      },
      {
        "mac": {"value": "11:22:33:44:55:66"},
        "name": {"value": "Laptop"},
        "ip": {"value": "192.168.1.102"}
      }
    ]
  },
  "id": 7
}
```

### 8. asus_router_get_client_full_info
Get complete information about a specific client by MAC address.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "asus_router_get_client_full_info",
  "params": {
    "mac": "AA:BB:CC:DD:EE:FF"
  },
  "id": 8
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "mac": {"value": "AA:BB:CC:DD:EE:FF"},
    "name": {"value": "Phone"},
    "ip": {"value": "192.168.1.101"},
    "isOnline": true,
    "isWired": false,
    "rssi": -45,
    "txRate": 866,
    "rxRate": 866,
    "connectionType": "5G",
    "vendor": "Apple"
  },
  "id": 8
}
```

### 9. asus_router_get_client_info_summary
Get summary information about a specific client.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "asus_router_get_client_info_summary",
  "params": {
    "mac": "AA:BB:CC:DD:EE:FF"
  },
  "id": 9
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "mac": {"value": "AA:BB:CC:DD:EE:FF"},
    "name": {"value": "Phone"},
    "ip": {"value": "192.168.1.101"},
    "isOnline": true,
    "isWired": false,
    "connectionType": "5G",
    "rssi": -45
  },
  "id": 9
}
```

### 10. asus_router_get_traffic
Get network traffic statistics with current speed.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "asus_router_get_traffic",
  "params": {},
  "id": 10
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "receivedBytes": 1073741824,
    "sentBytes": 536870912,
    "receivedSpeed": 1048576,
    "sentSpeed": 524288
  },
  "id": 10
}
```

## Command Line Usage

### PowerShell Examples

**List all tools:**
```powershell
echo '{"jsonrpc":"2.0","method":"tools/list","params":{},"id":1}' | java -jar asus-router-mcp-server-1.0.0-SNAPSHOT.jar
```

**Get uptime:**
```powershell
echo '{"jsonrpc":"2.0","method":"asus_router_get_uptime","params":{},"id":1}' | java -jar asus-router-mcp-server-1.0.0-SNAPSHOT.jar
```

**Get client info:**
```powershell
echo '{"jsonrpc":"2.0","method":"asus_router_get_client_full_info","params":{"mac":"AA:BB:CC:DD:EE:FF"},"id":1}' | java -jar asus-router-mcp-server-1.0.0-SNAPSHOT.jar
```

### Batch Processing

```powershell
# Multiple requests
@(
  '{"jsonrpc":"2.0","method":"asus_router_is_alive","params":{},"id":1}'
  '{"jsonrpc":"2.0","method":"asus_router_get_uptime","params":{},"id":2}'
  '{"jsonrpc":"2.0","method":"asus_router_get_memory_usage","params":{},"id":3}'
) | ForEach-Object {
  echo $_ | java -jar asus-router-mcp-server-1.0.0-SNAPSHOT.jar
}
```

## Error Codes

The server returns standard JSON-RPC 2.0 error codes plus custom router error codes:

| Code | Message | Description |
|------|---------|-------------|
| -32700 | Parse error | Invalid JSON |
| -32600 | Invalid Request | Invalid JSON-RPC structure |
| -32601 | Method not found | Unknown MCP tool |
| -32602 | Invalid params | Invalid parameters |
| -32603 | Internal error | Server error |
| -32001 | ROUTER_AUTH_FAILED | Router authentication failed |
| -32002 | ROUTER_COMM_ERROR | Router communication error |
| -32003 | ROUTER_PARSE_ERROR | Failed to parse router response |
| -32004 | CLIENT_NOT_FOUND | Client MAC address not found |
| -32005 | INVALID_RESPONSE | Invalid router response |
| -32006 | NETWORK_TIMEOUT | Network timeout |
| -32007 | INVALID_COMMAND | Invalid NVRAM command |
| -32008 | INVALID_PARAMETER | Invalid parameter value |

## Integration with AI Assistants

### Example: Claude Desktop Integration

Add to Claude Desktop configuration:

```json
{
  "mcpServers": {
    "asus-router": {
      "command": "java",
      "args": [
        "-jar",
        "C:/path/to/asus-router-mcp-server-1.0.0-SNAPSHOT.jar"
      ],
      "env": {
        "ASUS_ROUTER_HOST": "192.168.1.1",
        "ASUS_ROUTER_USERNAME": "admin",
        "ASUS_ROUTER_PASSWORD": "your_password"
      }
    }
  }
}
```

### Example: Custom Python Client

```python
import json
import subprocess

def call_mcp_tool(method, params=None):
    request = {
        "jsonrpc": "2.0",
        "method": method,
        "params": params or {},
        "id": 1
    }
    
    process = subprocess.Popen(
        ["java", "-jar", "asus-router-mcp-server-1.0.0-SNAPSHOT.jar"],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE
    )
    
    stdout, stderr = process.communicate(json.dumps(request).encode())
    return json.loads(stdout.decode())

# Usage
response = call_mcp_tool("asus_router_get_uptime")
print(f"Uptime: {response['result']['uptimeSeconds']} seconds")
```

## Best Practices

1. **Always include jsonrpc field** - Must be "2.0"
2. **Use unique IDs** - Helps correlate requests with responses
3. **Handle errors gracefully** - Check for error field in response
4. **Validate MAC addresses** - Use format AA:BB:CC:DD:EE:FF or AA-BB-CC-DD-EE-FF
5. **Set timeouts** - Router may be slow to respond
6. **Cache results** - Avoid excessive router queries
7. **Use notifications for fire-and-forget** - Omit ID field if no response needed

## Troubleshooting

**Q: Getting "Method not found" error**
- Verify method name matches exactly (case-sensitive)
- Check method is in tools/list response

**Q: Getting "ROUTER_AUTH_FAILED" error**
- Check router credentials in configuration
- Verify router admin password hasn't changed

**Q: No response from server**
- Check server is running and not crashed
- Verify JSON syntax is valid
- Check router connectivity

**Q: Getting "CLIENT_NOT_FOUND" error**
- Verify MAC address format is correct
- Check client is actually connected to router
- Try getting online clients list first

## Reference

- JSON-RPC 2.0 Specification: https://www.jsonrpc.org/specification
- Model Context Protocol: https://modelcontextprotocol.io/
- Project README: See README.md for full documentation
