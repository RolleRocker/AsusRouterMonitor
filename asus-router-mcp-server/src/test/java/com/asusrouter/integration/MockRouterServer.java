package com.asusrouter.integration;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Mock ASUS Router HTTP server for integration testing.
 * Simulates router HTTP API endpoints with realistic responses.
 */
public class MockRouterServer {
    
    private static final Logger log = Logger.getLogger(MockRouterServer.class.getName());
    
    private HttpServer server;
    private final int port;
    private final String username;
    private final String password;
    private final Map<String, String> sessionTokens = new HashMap<>();
    private boolean authenticationRequired = true;
    
    public MockRouterServer(int port, String username, String password) {
        this.port = port;
        this.username = username;
        this.password = password;
    }
    
    /**
     * Start the mock router server.
     * Includes retry logic for port binding (handles TIME_WAIT state).
     */
    public void start() throws IOException {
        IOException lastException = null;
        int maxRetries = 5;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
                
                // Login endpoint
                server.createContext("/login.cgi", new LoginHandler());
                
                // AppGet endpoint (main API)
                server.createContext("/appGet.cgi", new AppGetHandler());
                
                server.setExecutor(null); // Use default executor
                server.start();
                log.info("Mock router server started on port " + port);
                return; // Success!
            } catch (IOException e) {
                lastException = e;
                if (attempt < maxRetries) {
                    long waitTime = 1000 * attempt; // Progressive backoff: 1s, 2s, 3s, 4s
                    log.warning("Failed to bind to port " + port + " (attempt " + attempt + "), retrying in " + waitTime + "ms...");
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted while waiting to retry port binding", ie);
                    }
                }
            }
        }
        
        throw new IOException("Failed to start mock router after " + maxRetries + " attempts", lastException);
    }
    
    /**
     * Stop the mock router server.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            log.info("Mock router server stopped");
        }
    }
    
    /**
     * Disable authentication requirement for testing.
     */
    public void disableAuthentication() {
        this.authenticationRequired = false;
    }
    
    /**
     * Enable authentication requirement.
     */
    public void enableAuthentication() {
        this.authenticationRequired = true;
    }
    
    /**
     * Handle /login.cgi requests.
     */
    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            
            String loginUsername = params.get("login_username");
            String loginPassword = params.get("login_authorization");
            
            if (username.equals(loginUsername) && password.equals(loginPassword)) {
                // Generate session token
                String token = "mock_token_" + System.currentTimeMillis();
                sessionTokens.put(token, loginUsername);
                
                // Return success with token (asus_token cookie)
                String response = token;
                exchange.getResponseHeaders().add("Set-Cookie", "asus_token=" + token);
                sendResponse(exchange, 200, response);
                log.fine("Login successful for user: " + loginUsername);
            } else {
                sendResponse(exchange, 401, "error");
                log.fine("Login failed for user: " + loginUsername);
            }
        }
    }
    
    /**
     * Handle /appGet.cgi requests (main API).
     */
    private class AppGetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Check authentication
            if (authenticationRequired && !isAuthenticated(exchange)) {
                sendResponse(exchange, 401, "error");
                return;
            }
            
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String hook = params.get("hook");
            
            if (hook == null) {
                sendResponse(exchange, 400, "error");
                return;
            }
            
            String response = generateResponse(hook, params);
            sendResponse(exchange, 200, response);
            log.fine("Handled hook: " + hook + " -> " + response.substring(0, Math.min(50, response.length())));
        }
    }
    
    /**
     * Check if request is authenticated.
     * Supports both HTTP Basic Auth and cookie-based authentication.
     */
    private boolean isAuthenticated(HttpExchange exchange) {
        // Check HTTP Basic Auth
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring(6);
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes, StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);
            if (parts.length == 2 && username.equals(parts[0]) && password.equals(parts[1])) {
                return true;
            }
        }
        
        // Check cookie-based auth
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null) return false;
        
        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.trim().split("=");
            if (parts.length == 2 && "asus_token".equals(parts[0])) {
                return sessionTokens.containsKey(parts[1]);
            }
        }
        return false;
    }
    
    /**
     * Generate mock response based on hook.
     */
    private String generateResponse(String hook, Map<String, String> params) {
        return switch (hook) {
            case "uptime" -> "Thu, 09 Dec 2025 22:30:00 +0100;450123";
            
            case "memory_usage" -> "262144;107320;154824";
            
            case "cpu_usage" -> "38106047;3395512;38106008;2384694";
            
            case "netdev" -> "{\"eth0\":{\"tx_bytes\":256000000,\"rx_bytes\":192000000,\"tx_speed\":10.5,\"rx_speed\":15.3}}";
            
            case "wan_status" -> "{\"status\":\"connected\",\"statusCode\":1,\"wanIP\":\"192.168.1.100\",\"gateway\":\"192.168.1.1\",\"netmask\":\"255.255.255.0\",\"dns\":[\"8.8.8.8\",\"8.8.4.4\"]}";
            
            case "onlinelist" -> "[{\"mac\":\"AA:BB:CC:DD:EE:01\",\"ip\":\"192.168.1.101\"},{\"mac\":\"AA:BB:CC:DD:EE:02\",\"ip\":\"192.168.1.102\"},{\"mac\":\"AA:BB:CC:DD:EE:03\",\"ip\":\"192.168.1.103\"}]";
            
            case "dhcp_leases" -> "[{\"mac\":\"AA:BB:CC:DD:EE:01\",\"ip\":\"192.168.1.101\",\"hostname\":\"Client-1\",\"expires\":\"86400\"},{\"mac\":\"AA:BB:CC:DD:EE:02\",\"ip\":\"192.168.1.102\",\"hostname\":\"Client-2\",\"expires\":\"86400\"},{\"mac\":\"AA:BB:CC:DD:EE:03\",\"ip\":\"192.168.1.103\",\"hostname\":\"Client-3\",\"expires\":\"86400\"}]";
            
            case "get_clientlist" -> {
                String formatParam = params.get("parameter");
                int format = (formatParam != null) ? Integer.parseInt(formatParam) : 0;
                yield switch (format) {
                    case 0 -> // Basic format - simple list
                        "{\"AA:BB:CC:DD:EE:01\":{\"mac\":\"AA:BB:CC:DD:EE:01\",\"ip\":\"192.168.1.101\",\"name\":\"Device-1\",\"isOnline\":\"1\"},\"AA:BB:CC:DD:EE:02\":{\"mac\":\"AA:BB:CC:DD:EE:02\",\"ip\":\"192.168.1.102\",\"name\":\"Device-2\",\"isOnline\":\"1\"},\"AA:BB:CC:DD:EE:03\":{\"mac\":\"AA:BB:CC:DD:EE:03\",\"ip\":\"192.168.1.103\",\"name\":\"Device-3\",\"isOnline\":\"1\"}}";
                    case 1 -> // Detailed format - includes connection type and signal strength
                        "{\"AA:BB:CC:DD:EE:01\":{\"mac\":\"AA:BB:CC:DD:EE:01\",\"ip\":\"192.168.1.101\",\"name\":\"Device-1\",\"isOnline\":\"1\",\"isWL\":\"1\",\"type\":\"wireless\",\"rssi\":\"-65\",\"status\":\"Online\"},\"AA:BB:CC:DD:EE:02\":{\"mac\":\"AA:BB:CC:DD:EE:02\",\"ip\":\"192.168.1.102\",\"name\":\"Device-2\",\"isOnline\":\"1\",\"isWL\":\"1\",\"type\":\"wireless\",\"rssi\":\"-70\",\"status\":\"Online\"},\"AA:BB:CC:DD:EE:03\":{\"mac\":\"AA:BB:CC:DD:EE:03\",\"ip\":\"192.168.1.103\",\"name\":\"Device-3\",\"isOnline\":\"1\",\"isWL\":\"0\",\"type\":\"wired\",\"rssi\":\"0\",\"status\":\"Online\"}}";
                    case 2 -> // JSON format - full client info with get_clientlist wrapper
                        "{\"get_clientlist\":{\"AA:BB:CC:DD:EE:01\":{\"mac\":\"AA:BB:CC:DD:EE:01\",\"ip\":\"192.168.1.101\",\"name\":\"Device-1\",\"rssi\":\"-65\",\"isOnline\":\"1\",\"isWL\":\"1\",\"type\":\"wireless\"},\"AA:BB:CC:DD:EE:02\":{\"mac\":\"AA:BB:CC:DD:EE:02\",\"ip\":\"192.168.1.102\",\"name\":\"Device-2\",\"rssi\":\"-70\",\"isOnline\":\"1\",\"isWL\":\"1\",\"type\":\"wireless\"},\"AA:BB:CC:DD:EE:03\":{\"mac\":\"AA:BB:CC:DD:EE:03\",\"ip\":\"192.168.1.103\",\"name\":\"Device-3\",\"rssi\":\"0\",\"isOnline\":\"1\",\"isWL\":\"0\",\"type\":\"wired\"}}}";
                    default -> "{}"; 
                };
            }
            
            case "nvram_dump" -> "{\"lan_ipaddr\":\"192.168.1.1\",\"lan_netmask\":\"255.255.255.0\",\"lan_gateway\":\"192.168.1.1\",\"model\":\"RT-AX88U\",\"firmver\":\"3.0.0.4\",\"buildno\":\"386\",\"extendno\":\"45713\"}";
            
            case "nvram_get" -> {
                String nvramCommand = params.get("parameter");
                // Extract key name from full command (e.g., "nvram get lan_ipaddr" -> "lan_ipaddr")
                String nvramKey = nvramCommand;
                if (nvramCommand != null && nvramCommand.contains(" ")) {
                    String[] parts = nvramCommand.trim().split("\\s+");
                    // Expected format: "nvram get <key>" or "nvram_get <key>"
                    nvramKey = parts[parts.length - 1]; // Get last part which is the key
                }
                // NVRAM values are returned as plain text, not JSON
                yield switch (nvramKey != null ? nvramKey : "") {
                    case "lan_ipaddr" -> "192.168.1.1";
                    case "lan_netmask" -> "255.255.255.0";
                    case "lan_gateway" -> "192.168.1.1";
                    case "model" -> "RT-AX88U";
                    case "firmver" -> "3.0.0.4";
                    default -> "unknown";
                };
            }
            
            case "get_network_device_list" -> {
                String deviceName = params.get("parameter");
                if (deviceName != null && !deviceName.isEmpty()) {
                    yield "{\"name\":\"" + deviceName + "\",\"speed\":1000,\"status\":\"up\",\"mac\":\"AA:BB:CC:DD:EE:FF\"}";
                }
                yield "[\"eth0\",\"eth1\",\"eth2\",\"wlan0\",\"wlan1\"]";
            }
            
            case "get_wan_link" -> {
                String unitStr = params.get("parameter");
                Integer unit = (unitStr != null) ? Integer.parseInt(unitStr) : 0;
                // Return format: "status;interface" - simpler format
                yield unit == 0 ? "1;wan0" : "0;wan1";
            }
            
            case "is_alive" -> "1";
            
            default -> {
                log.warning("Unknown hook: " + hook);
                yield "error";
            }
        };
    }
    

    
    /**
     * Parse URL query string.
     */
    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    params.put(pair[0], pair[1]);
                }
            }
        }
        return params;
    }
    
    /**
     * Send HTTP response.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
