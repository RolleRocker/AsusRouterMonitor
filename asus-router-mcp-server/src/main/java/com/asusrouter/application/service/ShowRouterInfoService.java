package com.asusrouter.application.service;

import com.asusrouter.application.port.in.ShowRouterInfoUseCase;
import com.asusrouter.application.port.in.GetUptimeUseCase;
import com.asusrouter.application.port.in.GetMemoryUsageUseCase;
import com.asusrouter.application.port.in.GetCpuUsageUseCase;
import com.asusrouter.application.port.in.GetWanStatusUseCase;
import com.asusrouter.application.port.in.GetOnlineClientsUseCase;
import com.asusrouter.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use case implementation for displaying formatted router information.
 * Equivalent to Python's ShowRouterInfo functionality.
 */
@Service
@RequiredArgsConstructor
public class ShowRouterInfoService implements ShowRouterInfoUseCase {
    
    private final GetUptimeUseCase getUptimeUseCase;
    private final GetMemoryUsageUseCase getMemoryUsageUseCase;
    private final GetCpuUsageUseCase getCpuUsageUseCase;
    private final GetWanStatusUseCase getWanStatusUseCase;
    private final GetOnlineClientsUseCase getOnlineClientsUseCase;
    
    @Override
    public String execute(Boolean detailed) {
        boolean showDetailed = Boolean.TRUE.equals(detailed);
        
        StringBuilder output = new StringBuilder();
        output.append("═══════════════════════════════════════════════════════\n");
        output.append("           ASUS ROUTER MONITORING REPORT              \n");
        output.append("═══════════════════════════════════════════════════════\n\n");
        
        // System Information
        output.append("┌─ SYSTEM INFORMATION ─────────────────────────────────\n");
        formatUptime(output);
        formatMemoryUsage(output);
        formatCpuUsage(output);
        output.append("└──────────────────────────────────────────────────────\n\n");
        
        // Network Status
        output.append("┌─ NETWORK STATUS ─────────────────────────────────────\n");
        formatWanStatus(output);
        output.append("└──────────────────────────────────────────────────────\n\n");
        
        // Connected Clients
        output.append("┌─ CONNECTED CLIENTS ──────────────────────────────────\n");
        formatOnlineClients(output, showDetailed);
        output.append("└──────────────────────────────────────────────────────\n");
        
        return output.toString();
    }
    
    private void formatUptime(StringBuilder output) {
        try {
            Uptime uptime = getUptimeUseCase.execute();
            output.append("│ Uptime:       ").append(uptime.since()).append("\n");
            output.append("│ Duration:     ").append(formatDuration(uptime.getUptimeSeconds())).append("\n");
        } catch (Exception e) {
            output.append("│ Uptime:       ERROR - ").append(e.getMessage()).append("\n");
        }
    }
    
    private void formatMemoryUsage(StringBuilder output) {
        try {
            MemoryUsage memory = getMemoryUsageUseCase.execute();
            output.append("│ Memory:       ")
                  .append(String.format("%.1f%% used ", memory.getUsagePercentage()))
                  .append(String.format("(%d MB / %d MB)", 
                      memory.getUsedKB() / 1024, 
                      memory.getTotalKB() / 1024))
                  .append("\n");
        } catch (Exception e) {
            output.append("│ Memory:       ERROR - ").append(e.getMessage()).append("\n");
        }
    }
    
    private void formatCpuUsage(StringBuilder output) {
        try {
            CpuUsage cpu = getCpuUsageUseCase.execute();
            output.append("│ CPU Usage:    ")
                  .append(String.format("%.1f%% average ", cpu.getAveragePercentage()))
                  .append(String.format("(CPU1: %.1f%%, CPU2: %.1f%%)",
                      cpu.getCpu1Percentage(),
                      cpu.getCpu2Percentage()))
                  .append("\n");
        } catch (Exception e) {
            output.append("│ CPU Usage:    ERROR - ").append(e.getMessage()).append("\n");
        }
    }
    
    private void formatWanStatus(StringBuilder output) {
        try {
            WanStatus wan = getWanStatusUseCase.execute();
            output.append("│ Status:       ").append(wan.isConnected() ? "✓ Connected" : "✗ Disconnected").append("\n");
            output.append("│ WAN IP:       ").append(wan.ip().value()).append("\n");
            output.append("│ Gateway:      ").append(wan.gateway().value()).append("\n");
            output.append("│ Netmask:      ").append(wan.mask().value()).append("\n");
            output.append("│ DNS Servers:  ");
            if (!wan.dns().isEmpty()) {
                output.append(wan.dns().get(0).value());
                for (int i = 1; i < wan.dns().size(); i++) {
                    output.append(", ").append(wan.dns().get(i).value());
                }
            } else {
                output.append("None configured");
            }
            output.append("\n");
        } catch (Exception e) {
            output.append("│ Status:       ERROR - ").append(e.getMessage()).append("\n");
        }
    }
    
    private void formatOnlineClients(StringBuilder output, boolean detailed) {
        try {
            List<OnlineClient> clients = getOnlineClientsUseCase.execute();
            output.append("│ Total Online: ").append(clients.size()).append("\n");
            
            if (detailed && !clients.isEmpty()) {
                output.append("│\n");
                output.append("│ MAC Address       │ IP Address      \n");
                output.append("│ ──────────────────┼─────────────────\n");
                for (OnlineClient client : clients) {
                    output.append("│ ")
                          .append(String.format("%-17s", client.mac().normalized()))
                          .append(" │ ")
                          .append(String.format("%-15s", client.ip().value()))
                          .append("\n");
                }
            } else if (!detailed && !clients.isEmpty()) {
                output.append("│ (Use --detailed flag to see client list)\n");
            }
        } catch (Exception e) {
            output.append("│ Clients:      ERROR - ").append(e.getMessage()).append("\n");
        }
    }
    
    private String formatDuration(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (days > 0) {
            return String.format("%d days, %02d:%02d:%02d", days, hours, minutes, secs);
        } else {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        }
    }
}
