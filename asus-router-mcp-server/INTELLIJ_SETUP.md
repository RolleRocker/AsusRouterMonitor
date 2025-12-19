# IntelliJ IDEA Migration Guide

## ✅ Configuration Complete

Your project is now ready for IntelliJ IDEA with GitHub Copilot support!

## What Was Set Up

### 1. Code Style Configuration
- **Location**: `.idea/codeStyles/Project.xml`
- **Features**:
  - 4-space indentation (matching VS Code)
  - Import organization (java.* → javax.* → others)
  - 120-character line length
  - Consistent brace placement and spacing
  - Auto-formatting on save

### 2. Compiler Settings
- **Location**: `.idea/compiler.xml`
- **Features**:
  - Lombok annotation processing enabled
  - Java 21 bytecode target
  - Gradle annotation processor configuration

### 3. GitHub Copilot Instructions
- **Location**: `.idea/copilot/copilot-instructions.md`
- **Features**:
  - IntelliJ-specific keyboard shortcuts
  - Project architecture documentation
  - Testing patterns and conventions
  - Build and run commands
  - Common pitfalls and solutions

### 4. Git Configuration
- **Updated**: `.gitignore`
- **Features**:
  - Commits code style settings
  - Commits Copilot instructions
  - Ignores workspace-specific files (*.iml, out/, etc.)

## Opening the Project in IntelliJ

### First-Time Setup

1. **Launch IntelliJ IDEA**

2. **Open Project**
   - File → Open
   - Navigate to: `c:\dev\AsusRouterMonitor\asus-router-mcp-server`
   - Click OK

3. **Wait for Gradle Import**
   - IntelliJ will automatically detect Gradle
   - Wait for "Indexing..." to complete (bottom right)
   - Gradle sync runs automatically

4. **Verify JDK Configuration**
   - File → Project Structure (Ctrl+Alt+Shift+S)
   - Project → SDK: Java 21
   - If Java 21 is missing, click "Download JDK" and select version 21

5. **Verify Lombok Plugin**
   - File → Settings → Plugins
   - Search "Lombok"
   - Should be enabled by default in recent IntelliJ versions
   - If not, install and restart

6. **Enable GitHub Copilot**
   - File → Settings → Plugins
   - Search "GitHub Copilot"
   - Install if not present
   - Tools → GitHub Copilot → Login to GitHub

## Key Differences from VS Code

| Feature | VS Code | IntelliJ IDEA |
|---------|---------|---------------|
| **Project View** | Explorer (Ctrl+Shift+E) | Project (Alt+1) |
| **Find File** | Ctrl+P | Ctrl+Shift+N |
| **Find Class** | N/A | Ctrl+N |
| **Command Palette** | Ctrl+Shift+P | Ctrl+Shift+A |
| **Terminal** | Ctrl+` | Alt+F12 |
| **Run** | F5 | Shift+F10 |
| **Debug** | F5 | Shift+F9 |
| **Format Code** | Shift+Alt+F | Ctrl+Alt+L |
| **Go to Definition** | F12 | Ctrl+B |
| **Find Usages** | Shift+F12 | Alt+F7 |

## Building and Running

### Using Gradle Tool Window
1. Open Gradle window (right sidebar or View → Tool Windows → Gradle)
2. Expand `asus-router-mcp-server → Tasks`
3. Double-click tasks:
   - **build/build** - Full build with tests
   - **build/clean** - Clean outputs
   - **application/bootRun** - Run Spring Boot app

### Using Run Configurations

**MCP Server Mode:**
1. Open `AsusRouterMcpServerApplication.java`
2. Right-click class name → Run
3. Or click green arrow in gutter

**CLI Mode:**
1. Run → Edit Configurations
2. Click + → Application
3. Name: "Router CLI"
4. Main class: `com.asusrouter.AsusRouterMcpServerApplication`
5. Program arguments: `--cli`
6. Environment variables: `ASUS_ROUTER_PASSWORD=your_password`
7. Click OK and run

### Using Terminal
```cmd
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat bootRun
```

## GitHub Copilot Usage

### Inline Completion
- Type code, Copilot suggests completions
- **Tab** to accept
- **Alt+]** / **Alt+[** for next/previous suggestion

### Copilot Chat
- Open: View → Tool Windows → GitHub Copilot Chat
- Or: Ctrl+Shift+A → "GitHub Copilot Chat"
- Use `@workspace` to query entire project
- Reference files: `@WebClientConfig.java`

### Context Sources
Copilot reads these instructions from:
1. `.idea/copilot/copilot-instructions.md` (IntelliJ-specific)
2. `.github/copilot-instructions.md` (original)

Both are synchronized with project conventions!

## Testing in IntelliJ

### Running Tests
- Right-click test class/method → Run
- Use Test Runner window (Alt+4) for results
- Green bar = all tests pass
- Click failed test to see error details

### Test Coverage
1. Right-click test class → More Run/Debug → Run with Coverage
2. View coverage in Coverage window
3. Green highlights = covered code
4. Red highlights = not covered

### Debugging Tests
1. Set breakpoint (click left gutter, red dot appears)
2. Right-click test → Debug
3. Use Debug window (Alt+5)
4. Step through: F8 (step over), F7 (step into)

## Spring Boot Features

### Spring Tool Window
- View → Tool Windows → Spring
- See all beans, mappings, endpoints
- Click to navigate to source

### Endpoints View
- Navigate to controller method
- See HTTP endpoint mapping in gutter
- Click globe icon to test in HTTP Client

### HTTP Client Testing

**MCP Protocol Testing** (Recommended)

1. Open `src/test/resources/http-client/mcp-tools.http`
2. Verify `@baseUrl` matches your running server (default: `http://localhost:8080`)
3. Start the MCP server:
   ```cmd
   java -jar build\libs\asus-router-mcp-server-1.0.0-SNAPSHOT.jar
   ```
4. Click play button (▶) next to any HTTP request
5. Response shows in right panel with JSON syntax highlighting

**Router Direct API Testing** (Advanced)

1. Open `src/test/resources/http-client/router-api.http`
2. Edit `@routerHost`, `@routerUsername`, `@routerPassword` at the top
3. Click play button to test router endpoints directly
4. Useful for debugging router connectivity issues

**What You Can Test:**

- **MCP Protocol** (`mcp-tools.http`):
  - All 17 router monitoring tools via JSON-RPC 2.0
  - Tool discovery (`tools/list`)
  - Error handling (invalid methods, missing parameters)
  - Perfect for testing MCP server in isolation

- **Router Direct API** (`router-api.http`):
  - Raw router endpoints without MCP wrapper
  - Authentication and session management
  - Individual hook responses (uptime, memory, traffic, etc.)
  - Useful for understanding router response formats

**Pro Tips:**

- Use `Ctrl+Shift+F10` to run HTTP request in IntelliJ HTTP Client
- Use `Ctrl+Alt+Shift+R` to run recent HTTP request
- Set variables at top of `.http` file with `@varName = value`
- Use `>>>` to save response to file
- Chain requests: use `{{response.body.path}}` syntax

### Application Properties
- IntelliJ provides autocomplete for `application.yml`
- Ctrl+Space for property suggestions
- Ctrl+Click to navigate to source

## Productivity Tips

### Live Templates
Create common code patterns:
1. Settings → Editor → Live Templates
2. Create templates for:
   - `@McpTool` annotation
   - Record with validation
   - HTTP adapter pattern
   - Test method structure

### Multi-cursor Editing
- Alt+Click to add cursor
- Alt+Shift+Click to add cursor at line end
- Ctrl+Alt+Shift+J to select all occurrences

### Local History
- Right-click file → Local History → Show History
- View all changes even without Git
- Revert specific changes

### Code Analysis
- Analyze → Inspect Code
- Find potential bugs, code smells
- View inspection results
- Apply quick fixes (Alt+Enter)

## Troubleshooting

### Gradle Sync Issues
- File → Invalidate Caches → Invalidate and Restart
- Or: Gradle window → Reload All Gradle Projects (refresh icon)

### Lombok Not Working
- File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
- Check "Enable annotation processing"
- Rebuild project

### Tests Not Running
- Run → Edit Configurations → Templates → JUnit
- Set "Shorten command line" to "JAR manifest"
- Or use Gradle test task instead

### Code Style Not Applied
- Settings → Editor → Code Style → Java
- Scheme: "Project" (should be selected)
- If not, click gear icon → Import Scheme → IntelliJ IDEA code style XML

## Migration Checklist

- ✅ Project opened in IntelliJ
- ✅ Gradle sync completed successfully
- ✅ JDK 21 configured
- ✅ Lombok plugin enabled
- ✅ Annotation processing enabled
- ✅ GitHub Copilot installed and authenticated
- ✅ Code style applied (Ctrl+Alt+L to test)
- ✅ Tests run successfully
- ✅ Application runs (try both MCP and CLI modes)
- ✅ Copilot chat understands project context

## Next Steps

1. **Familiarize with shortcuts** - Print/bookmark the shortcuts reference
2. **Set up Run Configurations** - Create configs for MCP server and CLI
3. **Test with HTTP Client** - Open `mcp-tools.http` and test endpoints:
   - Start MCP server: `java -jar build\libs\asus-router-mcp-server-1.0.0-SNAPSHOT.jar`
   - Click play button next to any HTTP request
   - Verify JSON responses in right panel
4. **Explore Spring Tools** - Check Spring tool window for beans/endpoints
5. **Try Copilot Chat** - Ask questions about the architecture
6. **Run full build** - Ensure everything works: `.\gradlew.bat build`

## Resources

- IntelliJ IDEA Docs: https://www.jetbrains.com/idea/documentation/
- Spring Boot Support: https://www.jetbrains.com/help/idea/spring-boot.html
- GitHub Copilot in IntelliJ: https://plugins.jetbrains.com/plugin/17718-github-copilot
- Project Specification: `PROJECT_SPECIFICATION.md`
- Current Status: `STATUS.md`

---

**Questions?** Ask GitHub Copilot in the chat window! It now understands your project structure, architecture patterns, and IntelliJ-specific workflows.
