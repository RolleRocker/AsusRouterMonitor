package com.asusrouter.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.asusrouter.application.port.in.GetCpuUsageUseCase;
import com.asusrouter.application.port.in.GetMemoryUsageUseCase;
import com.asusrouter.application.port.in.GetOnlineClientsUseCase;
import com.asusrouter.application.port.in.GetTrafficUseCase;
import com.asusrouter.application.port.in.GetUptimeUseCase;
import com.asusrouter.application.port.in.GetWanStatusUseCase;
import com.asusrouter.application.port.in.IsAliveUseCase;
import com.asusrouter.domain.model.CpuUsage;
import com.asusrouter.domain.model.MemoryUsage;
import com.asusrouter.domain.model.OnlineClient;
import com.asusrouter.domain.model.TrafficWithSpeed;
import com.asusrouter.domain.model.Uptime;
import com.asusrouter.domain.model.WanStatus;

/**
 * Integration tests for ASUS Router MCP Server.
 * These tests require a real router connection and are disabled by default.
 * 
 * To enable: Set environment variable ROUTER_INTEGRATION_TEST=true
 * Configure router connection in application.yml or environment variables.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ROUTER_INTEGRATION_TEST", matches = "true")
class RouterIntegrationTest {
    
    @Autowired
    private IsAliveUseCase isAliveUseCase;
    
    @Autowired
    private GetUptimeUseCase getUptimeUseCase;
    
    @Autowired
    private GetMemoryUsageUseCase getMemoryUsageUseCase;
    
    @Autowired
    private GetCpuUsageUseCase getCpuUsageUseCase;
    
    @Autowired
    private GetWanStatusUseCase getWanStatusUseCase;
    
    @Autowired
    private GetOnlineClientsUseCase getOnlineClientsUseCase;
    
    @Autowired
    private GetTrafficUseCase getTrafficUseCase;
    
    @Test
    void shouldConnectToRouter() {
        boolean alive = isAliveUseCase.execute();
        assertTrue(alive, "Router should be reachable and responsive");
    }
    
    @Test
    void shouldGetRouterUptime() {
        Uptime uptime = getUptimeUseCase.execute();
        
        assertNotNull(uptime, "Uptime should not be null");
        assertTrue(uptime.getUptimeSeconds() > 0, "Uptime seconds should be positive");
        assertNotNull(uptime.since(), "Last boot time should be set");
    }
    
    @Test
    void shouldGetMemoryUsage() {
        MemoryUsage memory = getMemoryUsageUseCase.execute();
        
        assertNotNull(memory, "Memory usage should not be null");
        assertTrue(memory.getTotalKB() > 0, "Total memory should be positive");
        assertTrue(memory.getUsedKB() >= 0, "Used memory should be non-negative");
        assertTrue(memory.getFreeKB() >= 0, "Free memory should be non-negative");
        assertTrue(memory.getUsagePercentage() >= 0 && memory.getUsagePercentage() <= 100, 
            "Memory usage percent should be between 0 and 100");
    }
    
    @Test
    void shouldGetCpuUsage() {
        CpuUsage cpu = getCpuUsageUseCase.execute();
        
        assertNotNull(cpu, "CPU usage should not be null");
        assertTrue(cpu.getAveragePercentage() >= 0 && cpu.getAveragePercentage() <= 100,
            "Average CPU usage should be between 0 and 100");
        assertTrue(cpu.getCpu1Percentage() >= 0 && cpu.getCpu1Percentage() <= 100,
            "CPU1 usage should be between 0 and 100");
        assertTrue(cpu.getCpu2Percentage() >= 0 && cpu.getCpu2Percentage() <= 100,
            "CPU2 usage should be between 0 and 100");
    }
    
    @Test
    void shouldGetWanStatus() {
        WanStatus wan = getWanStatusUseCase.execute();
        
        assertNotNull(wan, "WAN status should not be null");
        assertNotNull(wan.status(), "WAN status string should not be null");
        assertNotNull(wan.ip(), "WAN IP should not be null");
        assertNotNull(wan.gateway(), "WAN gateway should not be null");
        assertNotNull(wan.mask(), "WAN mask should not be null");
        assertNotNull(wan.dns(), "WAN DNS list should not be null");
    }
    
    @Test
    void shouldGetOnlineClients() {
        List<OnlineClient> clients = getOnlineClientsUseCase.execute();
        
        assertNotNull(clients, "Online clients list should not be null");
        assertTrue(clients.size() >= 0, "Client count should be non-negative");
    }
    
    @Test
    void shouldGetTrafficStatistics() {
        TrafficWithSpeed traffic = getTrafficUseCase.execute();
        
        assertNotNull(traffic, "Traffic should not be null");
        assertNotNull(traffic.total(), "Traffic total should not be null");
        assertNotNull(traffic.speed(), "Traffic speed should not be null");
        
        assertTrue(traffic.total().sent() >= 0, "Sent MB should be non-negative");
        assertTrue(traffic.total().recv() >= 0, "Received MB should be non-negative");
        assertTrue(traffic.speed().sent() >= 0, "Sent speed should be non-negative");
        assertTrue(traffic.speed().recv() >= 0, "Received speed should be non-negative");
    }
}
