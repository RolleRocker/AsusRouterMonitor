# HTTP Client Testing Guide for IntelliJ IDEA

## Quick Start (2 minutes)

### 1. Start the MCP Server

Open terminal in IntelliJ (Alt+F12) and run:

```powershell
.\gradlew.bat bootRun
# or
java -jar build\libs\asus-router-mcp-server-1.0.0-SNAPSHOT.jar
```

Wait for the server to start (you'll see "Started AsusRouterMcpServerApplication").

### 2. Open HTTP Test File

1. In Project view (Alt+1), navigate to:
   `src/test/resources/http-client/mcp-tools.http`
2. Right-click → Open → IntelliJ HTTP Client

### 3. Run Your First Test

Click the ▶ play button next to this request:

```
### 1. Tools List - Discover all available tools
POST {{baseUrl}}/mcp
...
```

### 4. View Response

Response appears in the right panel with syntax highlighting.

**Expected response:**

```json
{
  "jsonrpc": "2.0",
  "result": {
    "tools": [
      {
        "name": "asus_router_get_uptime",
        "description": "Retrieve router uptime information"
      },
      ...
    ]
  },
  "id": 1
}
```

## What You Can Test

### Category 1: System Monitoring (No Parameters)

These tools require no parameters and work with mock server:

```
### 2. Get Router Uptime
### 3. Check Router Alive Status
### 4. Get Memory Usage
### 5. Get CPU Usage
### 6. Get Traffic Total
### 7. Get WAN Status
### 8. Get Online Clients
### 9. Get Traffic (detailed by interface)
### 12. Get DHCP Leases
### 13. Get Router Settings
### 14. Get NVRAM Settings
### 15. Get Client List
### 16. Get Network Device List
### 17. Get WAN Link Info
### 18. Show Router Info
```

**How to test:** Click ▶ next to any request above.

### Category 2: Client-Specific Tools (Requires MAC Address)

These tools take parameters. Edit the MAC address before testing:

```
### 10. Get Client Full Info
{
  "params": {
    "mac": "AA:BB:CC:DD:EE:FF"  ← Change this to a real client MAC
  }
}

### 11. Get Client Info Summary
{
  "params": {
    "mac": "AA:BB:CC:DD:EE:FF"  ← Change this to a real client MAC
  }
}
```

**How to test:**
1. Get a real client MAC from online clients list (test #8)
2. Copy the MAC address
3. Paste into test #10 or #11
4. Click ▶ to run

### Category 3: Error Handling Tests

These test error scenarios:

```
### Error Handling Test - Invalid method
### Error Handling Test - Missing required parameter
```

**Expected behavior:** Returns JSON-RPC error response.

## Testing Workflow

### Test 1: Verify Server is Running

```
### 1. Tools List
```

- Should return list of 17 tools
- If fails: Check server is running (see "Quick Start" section)

### Test 2: Test System Monitoring

```
### 2. Get Router Uptime
```

- Should return uptime data
- Example: `"uptime": "0 days 5 hours 23 minutes"`

### Test 3: Test List Endpoints

```
### 8. Get Online Clients
```

- Returns list of connected devices
- Copy one MAC address for next test

### Test 4: Test Client-Specific Endpoint

```
### 10. Get Client Full Info
```

- Paste the MAC from previous test
- Should return detailed client information

### Test 5: Test Error Handling

```
### Error Handling Test - Invalid method
```

- Should return error response with `"error"` field
- Code: `-32601` (Method not found)

## Advanced Testing

### Chaining Requests

Use response variables to chain requests:

```
### Get Online Clients
POST {{baseUrl}}/mcp
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "asus_router_get_online_clients",
  "params": {},
  "id": 8
}

> {% 
  client.global.set("firstClientMac", response.body.result.clients[0].mac);
%}

### Get Info for First Client
POST {{baseUrl}}/mcp
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "asus_router_get_client_full_info",
  "params": {
    "mac": "{{firstClientMac}}"
  },
  "id": 10
}
```

### Save Responses to File

Add this after a request to save the response:

```
>>> response-{{_now.unix_time}}.json
```

This saves each response with a unique timestamp.

### Environment-Specific Testing

Create multiple `.http` files for different environments:

- `mcp-tools-localhost.http` (development)
- `mcp-tools-staging.http` (staging server)
- `mcp-tools-production.http` (production)

Change `@baseUrl` at the top of each file.

## Testing with Real Router

### Option 1: Direct Router API Testing

For testing without the MCP layer:

1. Open `src/test/resources/http-client/router-api.http`
2. Edit these variables at the top:
   ```
   @routerHost = 192.168.1.1        ← Your router IP
   @routerUsername = admin          ← Your username
   @routerPassword = your_password  ← Your password
   ```
3. Click ▶ next to any request

### Option 2: Configure Application to Use Real Router

1. Edit `src/main/resources/application.yml`:
   ```yaml
   asus:
     router:
       host: "192.168.1.1"           # Your router
       username: "admin"
       password: "${ASUS_ROUTER_PASSWORD}"  # Set env var or use plaintext
   ```

2. Or set environment variables:
   ```powershell
   $env:ASUS_ROUTER_HOST="192.168.1.1"
   $env:ASUS_ROUTER_USERNAME="admin"
   $env:ASUS_ROUTER_PASSWORD="your_password"
   ```

3. Restart the MCP server

4. Run HTTP tests - they'll now hit your real router

## Troubleshooting

### "Connection refused" Error

**Problem:** `Failed to connect to http://localhost:8080`

**Solution:**
1. Check MCP server is running: `.\gradlew.bat bootRun`
2. Wait 5-10 seconds after starting
3. Verify port 8080 is not in use: `netstat -ano | findstr :8080`

### "404 Not Found" Error

**Problem:** Request returns 404

**Solution:**
1. Verify `@baseUrl` is correct at top of `.http` file
2. Check server logs for routing issues
3. Ensure server is fully started

### "401 Unauthorized" Error

**Problem:** Router returns 401 when testing direct API

**Solution:**
1. Verify router credentials in `router-api.http`
2. Check router is reachable: `ping 192.168.1.1`
3. Confirm user account has admin privileges

### "Connection timeout" Error

**Problem:** Request hangs and eventually times out

**Solution:**
1. Check server is responsive: Use "Tools List" test
2. If timeout, server may be stuck: Restart it
3. Check system resources (CPU, memory)

## Tips & Tricks

### Using Ctrl+Shift+F10

Keyboard shortcut to run HTTP request without mouse:

1. Place cursor anywhere in the request block
2. Press `Ctrl+Shift+F10`
3. Response appears in right panel

### Recent Requests

Use `Ctrl+Alt+Shift+R` to run the most recently executed HTTP request.

### Copy as Curl

Right-click response → Copy as Curl to use in terminal:

```bash
curl -X POST http://localhost:8080/mcp \
  -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0",...}'
```

### Pretty Print JSON

Right-click response → Pretty Print to format JSON for readability.

### Save Test Results

Right-click response panel → Save response to file for documentation.

## Next Steps

1. ✅ Run "Tools List" test (#1)
2. ✅ Run "Get Uptime" test (#2)
3. ✅ Run "Get Online Clients" test (#8)
4. ✅ Run "Error Handling" test to understand error responses
5. 🔧 Configure real router credentials
6. 🔧 Test with real router using `router-api.http`
7. 📊 Combine tests to validate end-to-end workflows

## Resources

- **IntelliJ HTTP Client Docs**: https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html
- **REST Client Extension Reference**: https://github.com/Huachao/vscode-restclient
- **MCP Protocol Spec**: `docs/MCP_PROTOCOL_USAGE.md`
- **Project Specification**: `PROJECT_SPECIFICATION.md`

