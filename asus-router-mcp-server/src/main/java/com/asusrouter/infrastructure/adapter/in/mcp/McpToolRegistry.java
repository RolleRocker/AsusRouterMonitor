package com.asusrouter.infrastructure.adapter.in.mcp;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runtime registry of all MCP tools.
 * Lists all available ASUS Router tools that can be invoked via MCP protocol.
 */
@Component
public class McpToolRegistry {
    
    /**
     * Get list of all available MCP tool names.
     * These correspond to the methods in McpJsonRpcHandler.
     */
    public List<String> getAllToolNames() {
        return List.of(
            "tools/list",
            "asus_router_get_uptime",
            "asus_router_is_alive",
            "asus_router_get_memory_usage",
            "asus_router_get_cpu_usage",
            "asus_router_get_traffic_total",
            "asus_router_get_wan_status",
            "asus_router_get_online_clients",
            "asus_router_get_traffic",
            "asus_router_get_client_full_info",
            "asus_router_get_client_info_summary",
            "asus_router_get_dhcp_leases",
            "asus_router_get_settings",
            "asus_router_get_nvram",
            "asus_router_get_client_list",
            "asus_router_get_network_device_list",
            "asus_router_get_wan_link",
            "asus_router_show_router_info"
        );
    }
    
    /**
     * Get description for a specific tool.
     */
    public String getToolDescription(String toolName) {
        return switch (toolName) {
            case "tools/list" -> "List all available MCP tools";
            case "asus_router_get_uptime" -> "Return uptime of the router with last boot time and uptime in seconds";
            case "asus_router_is_alive" -> "Check if router is online and responsive";
            case "asus_router_get_memory_usage" -> "Return current memory usage information including total, used, and free memory";
            case "asus_router_get_cpu_usage" -> "Return current CPU usage information including total usage and per-core breakdown";
            case "asus_router_get_traffic_total" -> "Retrieve total traffic statistics (received and transmitted bytes)";
            case "asus_router_get_wan_status" -> "Return WAN connection status including IP, gateway, DNS servers";
            case "asus_router_get_online_clients" -> "Retrieve list of currently online/connected clients with their basic information";
            case "asus_router_get_traffic" -> "Retrieve network traffic information including current speed and total data transferred";
            case "asus_router_get_client_full_info" -> "Retrieve complete information about a specific connected client including connection details, traffic statistics, and configuration";
            case "asus_router_get_client_info_summary" -> "Retrieve summary information about a specific connected client including name, IP, connection type, and signal strength";
            case "asus_router_get_dhcp_leases" -> "Retrieve DHCP lease information for clients that obtained IP addresses from the router's DHCP server";
            case "asus_router_get_settings" -> "Retrieve comprehensive router configuration settings including network, wireless, and system settings";
            case "asus_router_get_nvram" -> "Execute custom NVRAM commands to retrieve router configuration values. Caution: Incorrect usage can affect router stability.";
            case "asus_router_get_client_list" -> "Retrieve the list of connected clients with their detailed information";
            case "asus_router_get_network_device_list" -> "Retrieve list of network devices detected by the router, including both connected and known devices";
            case "asus_router_get_wan_link" -> "Retrieve WAN link information including connection type, status, and bandwidth statistics";
            case "asus_router_show_router_info" -> "Display formatted summary of router status including uptime, memory, CPU, WAN, and connected clients";
            default -> "Unknown tool";
        };
    }
}
