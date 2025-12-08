package com.asusrouter.cli;

import com.asusrouter.application.port.in.ShowRouterInfoUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Command-line runner for ShowRouterInfo functionality.
 * Equivalent to Python's ShowRouterInfo.py script.
 * 
 * Usage:
 *   java -jar asus-router-mcp-server.jar --cli
 *   java -jar asus-router-mcp-server.jar --cli --detailed
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShowRouterInfoRunner implements CommandLineRunner {
    
    private final ShowRouterInfoUseCase showRouterInfoUseCase;
    
    @Override
    public void run(String... args) throws Exception {
        boolean cliMode = false;
        boolean detailed = false;
        
        // Parse arguments
        for (String arg : args) {
            if (arg.equals("--cli") || arg.equals("--show-info")) {
                cliMode = true;
            } else if (arg.equals("--detailed") || arg.equals("-d")) {
                detailed = true;
            }
        }
        
        if (!cliMode) {
            // Not in CLI mode, skip
            return;
        }
        
        log.info("Running ShowRouterInfo CLI...");
        
        try {
            String output = showRouterInfoUseCase.execute(detailed);
            System.out.println(output);
            
            // Exit cleanly after displaying info
            System.exit(0);
            
        } catch (Exception e) {
            log.error("Error retrieving router information", e);
            System.err.println("\n❌ ERROR: " + e.getMessage());
            System.err.println("\nPlease check:");
            System.err.println("  1. Router is accessible at configured host/port");
            System.err.println("  2. Credentials are correct in application.yml");
            System.err.println("  3. Router HTTP API is enabled");
            System.exit(1);
        }
    }
}
