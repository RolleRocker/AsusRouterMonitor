package com.asusrouter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main Spring Boot application class for ASUS Router MCP Server.
 * 
 * This application provides a Model Context Protocol (MCP) server interface
 * for monitoring and managing ASUS routers. It exposes 17 MCP tools that
 * translate the Python RouterInfo library functionality to Java.
 * 
 * Architecture:
 * - Domain Layer: Pure business logic and entities (no framework dependencies)
 * - Application Layer: Use cases implementing business logic (port interfaces)
 * - Infrastructure Layer: Adapters for HTTP (router communication) and MCP (stdio protocol)
 * 
 * Key Features:
 * - Hexagonal (Ports & Adapters) architecture
 * - Compile-time annotation processing for MCP tool schema generation
 * - Reactive HTTP client (Spring WebFlux) for router communication
 * - JSON-RPC 2.0 over stdio for MCP protocol
 * 
 * Usage:
 * - As MCP Server: Launched by AI assistants via stdio communication
 * - As CLI: ShowRouterInfo command-line runner for direct monitoring
 * 
 * Configuration:
 * - Router connection settings in application.yml
 * - See RouterProperties for all available configuration options
 */
@SpringBootApplication
@EnableConfigurationProperties
public class AsusRouterMcpServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AsusRouterMcpServerApplication.class, args);
    }
}
