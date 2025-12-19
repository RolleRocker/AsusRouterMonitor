package com.asusrouter.infrastructure.adapter.in.mcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcError;
import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcRequest;
import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        
        // System.out is intentionally used here for MCP protocol communication (not logging)
        @SuppressWarnings("squid:S106") // System.out required for MCP JSON-RPC 2.0 protocol
        PrintWriter writer = new PrintWriter(System.out, true);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             writer) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty()) {
                    continue;
                }
                
                log.debug("Received: {}", line);
                
                processRequest(line, writer);
            }
            
            log.info("MCP stdio transport shutting down");
            
        } catch (Exception e) {
            log.error("Fatal error in MCP stdio transport", e);
            throw e;
        }
    }
    
    /**
     * Process a single JSON-RPC request.
     * Extracted method to improve code structure and testability.
     */
    private void processRequest(String line, PrintWriter writer) {
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
            
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("JSON parsing error: {}", line, e);
            sendErrorResponse(writer, e.getMessage());
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", line, e);
            sendErrorResponse(writer, e.getMessage());
            
        } catch (Exception e) {
            log.error("Unexpected error processing request: {}", line, e);
            sendErrorResponse(writer, e.getMessage());
        }
    }
    
    /**
     * Send an error response to the client.
     */
    private void sendErrorResponse(PrintWriter writer, String errorMessage) {
        try {
            JsonRpcResponse errorResponse = JsonRpcResponse.error(
                JsonRpcError.parseError(errorMessage),
                null
            );
            
            String errorJson = objectMapper.writeValueAsString(errorResponse);
            writer.println(errorJson);
            writer.flush();
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to send error response", e);
        }
    }
}
