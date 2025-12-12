package com.asusrouter.infrastructure.adapter.in.mcp;

import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcRequest;
import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcResponse;
import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * MCP stdio transport layer.
 * Reads JSON-RPC requests from stdin, writes responses to stdout.
 * This is the primary interface for AI assistant integration.
 * 
 * Note: Disabled during Spring Boot tests (profile != test) to prevent
 * stream closed errors when stdin is unavailable.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class McpStdioTransport implements CommandLineRunner {
    
    private final ObjectMapper objectMapper;
    private final McpJsonRpcHandler jsonRpcHandler;
    
    @Override
    public void run(String... args) throws Exception {
        // Check if we should run as MCP server (default) or skip for other modes
        boolean mcpMode = true;
        
        for (String arg : args) {
            if (arg.equals("--cli") || arg.equals("--show-info")) {
                mcpMode = false;
                break;
            }
        }
        
        if (!mcpMode) {
            log.info("Skipping MCP stdio mode (CLI mode detected)");
            return;
        }
        
        log.info("Starting MCP stdio transport...");
        log.info("Listening for JSON-RPC 2.0 requests on stdin");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(System.out, true)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty()) {
                    continue;
                }
                
                log.debug("Received: {}", line);
                
                try {
                    // Parse JSON-RPC request
                    JsonRpcRequest request = objectMapper.readValue(line, JsonRpcRequest.class);
                    
                    // Handle request
                    JsonRpcResponse response = jsonRpcHandler.handleRequest(request);
                    
                    // Write response to stdout
                    String responseJson = objectMapper.writeValueAsString(response);
                    writer.println(responseJson);
                    writer.flush();
                    
                    log.debug("Sent: {}", responseJson);
                    
                } catch (Exception e) {
                    log.error("Error processing request: {}", line, e);
                    
                    // Send error response using static factory method
                    JsonRpcResponse errorResponse = JsonRpcResponse.error(
                        JsonRpcError.parseError(e.getMessage()),
                        null
                    );
                    
                    String errorJson = objectMapper.writeValueAsString(errorResponse);
                    writer.println(errorJson);
                    writer.flush();
                }
            }
            
            log.info("MCP stdio transport shutting down");
            
        } catch (Exception e) {
            log.error("Fatal error in MCP stdio transport", e);
            throw e;
        }
    }
}
