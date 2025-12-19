# Quick Start Visual Guide

## 🚀 Get Started in 5 Minutes

### 1️⃣ Build the Project (1 minute)

```powershell
cd C:\dev\AsusRouterMonitor\asus-router-mcp-server
.\gradlew.bat build
```

**Expected Output:**
```
BUILD SUCCESSFUL in Xs
```

### 2️⃣ Start the MCP Server (30 seconds)

```powershell
java -jar build\libs\asus-router-mcp-server-1.0.0-SNAPSHOT.jar
```

**Expected Output:**
```
Started AsusRouterMcpServerApplication in X.XXX seconds (process running)
```

Leave this running! ⏱️

### 3️⃣ Open IntelliJ IDEA (1 minute)

```
1. File → Open
2. Navigate to: C:\dev\AsusRouterMonitor\asus-router-mcp-server
3. Wait for Gradle sync
4. Wait for "Indexing..." to complete
```

### 4️⃣ Open HTTP Test File (30 seconds)

In IntelliJ, go to:
```
src/test/resources/http-client/mcp-tools.http
```

### 5️⃣ Run Your First Test (30 seconds)

Find this section in the file:
```
### 1. Tools List - Discover all available tools
POST {{baseUrl}}/mcp
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "tools/list",
  "params": {},
  "id": 1
}
```

Click the ▶ play button on the left.

**Expected Response:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "tools": [
      {
        "name": "asus_router_get_uptime",
        "description": "Retrieve router uptime information"
      },
      ... (17 tools total)
    ]
  },
  "id": 1
}
```

## 🎯 What You Can Do Now

### Test All 17 Tools

```
Request #2  → Get Uptime
Request #3  → Check Alive
Request #4  → Memory Usage
Request #5  → CPU Usage
Request #6  → Traffic Total
Request #7  → WAN Status
Request #8  → Online Clients (copy MAC from response)
Request #9  → Traffic (detailed)
Request #10 → Client Full Info (paste MAC here)
Request #11 → Client Info Summary
Request #12 → DHCP Leases
Request #13 → Settings
Request #14 → NVRAM
Request #15 → Client List
Request #16 → Network Devices
Request #17 → WAN Link
Request #18 → Show Router Info
```

### Test Error Handling

```
Request #19 → Invalid Method (error test)
Request #20 → Missing Parameter (error test)
```

## 📚 Documentation

| Want to Know... | Read This |
|---|---|
| How to test everything | `docs/HTTP_CLIENT_TESTING.md` |
| IntelliJ setup & shortcuts | `INTELLIJ_SETUP.md` |
| Architecture & design | `PROJECT_SPECIFICATION.md` |
| Project status | `STATUS.md` |
| All completed work | `CHECKLIST.md` |
| MCP protocol details | `docs/MCP_PROTOCOL_USAGE.md` |

## ⌨️ Keyboard Shortcuts

| Shortcut | Action |
|---|---|
| `Ctrl+Shift+F10` | Run HTTP request |
| `Ctrl+Alt+Shift+R` | Run recent request |
| `Alt+F12` | Open terminal |
| `Ctrl+Alt+L` | Format JSON response |
| `Alt+1` | Show Project view |
| `Ctrl+Alt+Shift+S` | Project Structure |

## 🔧 Common Tasks

### Run CLI Mode
```powershell
# Terminal 1: Stop MCP server (Ctrl+C)
# Terminal 2:
java -jar build\libs\asus-router-mcp-server-1.0.0-SNAPSHOT.jar --cli
```

### Run Tests
```powershell
.\gradlew.bat test
```

### View Test Results
```
IntelliJ: View → Tool Windows → Test Results (Alt+4)
```

### Configure Real Router (Optional)

Edit `src/main/resources/application.yml`:
```yaml
asus:
  router:
    host: "192.168.1.1"        # Your router IP
    username: "admin"
    password: "your_password"
```

Then restart MCP server.

## 🐛 Troubleshooting

### "Connection refused" Error
**Problem:** Tests won't connect to server
```
✓ Check: Is MCP server running in terminal?
✓ Check: Does it say "Started AsusRouterMcpServerApplication"?
✓ Fix: Restart the MCP server
```

### "404 Not Found" Error
**Problem:** Request returns 404
```
✓ Check: baseUrl is http://localhost:8080
✓ Check: Server is fully started (wait 5 seconds)
✓ Fix: Restart IntelliJ and try again
```

### Tests Already Passing
**If you see:** "BUILD SUCCESSFUL" immediately
```
✓ Good news: Tests are cached and passing
✓ To force re-run: .\gradlew.bat clean build
```

## 📋 Quick Checklist

- [ ] Build succeeds: `.\gradlew.bat build`
- [ ] JAR exists: `build\libs\asus-router-mcp-server-1.0.0-SNAPSHOT.jar`
- [ ] MCP server starts: No errors in terminal
- [ ] IntelliJ opens project: Gradle sync completes
- [ ] HTTP file visible: `src/test/resources/http-client/mcp-tools.http`
- [ ] First test works: "Tools List" returns 17 tools
- [ ] You can see response: Right panel shows JSON

## 🎉 Success Criteria

✅ When you see this, you're good to go:
- MCP server running in terminal
- IntelliJ open with project loaded
- `mcp-tools.http` file visible
- Clicking ▶ shows JSON response
- Response contains "tools" array with 17 entries

## 🚀 Next Steps

1. Run all 18 tool tests in sequence
2. Copy MAC from "Online Clients" test
3. Use MAC in "Client Full Info" test
4. Try error handling tests
5. Read `docs/HTTP_CLIENT_TESTING.md` for advanced usage
6. Configure real router for production testing

## 💡 Pro Tips

- **Right-click response** → Copy as Curl to use in terminal
- **Set variables** at top of `.http` file (like `@baseUrl`)
- **Use `>>>` to save** responses to files for documentation
- **Keyboard shortcut** `Ctrl+Shift+F10` faster than clicking ▶
- **Comment requests** with `###` for organization

## 📞 Need Help?

1. Read the relevant documentation file (see table above)
2. Check troubleshooting section
3. Ask GitHub Copilot in IntelliJ Chat window
4. Review Project Specification for architecture details

---

**That's it! You're ready to test the ASUS Router MCP Server! 🎉**

Start with the build command above and you'll be running tests in 5 minutes.

