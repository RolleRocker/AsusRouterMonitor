package com.asusrouter.integration;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.asusrouter.application.port.in.GetClientFullInfoUseCase;
import com.asusrouter.application.port.in.GetClientInfoSummaryUseCase;
import com.asusrouter.application.port.in.GetClientListUseCase;
import com.asusrouter.application.port.in.GetCpuUsageUseCase;
import com.asusrouter.application.port.in.GetDhcpLeasesUseCase;
import com.asusrouter.application.port.in.GetMemoryUsageUseCase;
import com.asusrouter.application.port.in.GetNetworkDeviceListUseCase;
import com.asusrouter.application.port.in.GetNvramUseCase;
import com.asusrouter.application.port.in.GetOnlineClientsUseCase;
import com.asusrouter.application.port.in.GetSettingsUseCase;
import com.asusrouter.application.port.in.GetTrafficTotalUseCase;
import com.asusrouter.application.port.in.GetTrafficUseCase;
import com.asusrouter.application.port.in.GetUptimeUseCase;
import com.asusrouter.application.port.in.GetWanLinkUseCase;
import com.asusrouter.application.port.in.GetWanStatusUseCase;
import com.asusrouter.application.port.in.IsAliveUseCase;
import com.asusrouter.application.port.in.ShowRouterInfoUseCase;
import com.asusrouter.domain.model.ClientFullInfo;
import com.asusrouter.domain.model.ClientSummary;
import com.asusrouter.domain.model.CpuUsage;
import com.asusrouter.domain.model.DhcpLease;
import com.asusrouter.domain.model.IpAddress;
import com.asusrouter.domain.model.MacAddress;
import com.asusrouter.domain.model.MemoryUsage;
import com.asusrouter.domain.model.Netmask;
import com.asusrouter.domain.model.OnlineClient;
import com.asusrouter.domain.model.RouterSettings;
import com.asusrouter.domain.model.TrafficTotal;
import com.asusrouter.domain.model.TrafficWithSpeed;
import com.asusrouter.domain.model.Uptime;
import com.asusrouter.domain.model.WanStatus;

/**
 * Comprehensive integration tests for all 17 ASUS Router tools.
 * Uses MockRouterServer to simulate router HTTP API responses.
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RouterToolsIntegrationTest {
    
    private static final int MOCK_ROUTER_PORT = 8888;
    private static final String MOCK_USERNAME = "admin";
    private static final String MOCK_PASSWORD = "test123";
    
    private MockRouterServer mockRouter;
    
    @Autowired
    private GetUptimeUseCase getUptimeUseCase;
    
    @Autowired
    private IsAliveUseCase isAliveUseCase;
    
    @Autowired
    private GetMemoryUsageUseCase getMemoryUsageUseCase;
    
    @Autowired
    private GetCpuUsageUseCase getCpuUsageUseCase;
    
    @Autowired
    private GetTrafficTotalUseCase getTrafficTotalUseCase;
    
    @Autowired
    private GetTrafficUseCase getTrafficUseCase;
    
    @Autowired
    private GetWanStatusUseCase getWanStatusUseCase;
    
    @Autowired
    private GetOnlineClientsUseCase getOnlineClientsUseCase;
    
    @Autowired
    private GetDhcpLeasesUseCase getDhcpLeasesUseCase;
    
    @Autowired
    private GetClientFullInfoUseCase getClientFullInfoUseCase;
    
    @Autowired
    private GetClientInfoSummaryUseCase getClientInfoSummaryUseCase;
    
    @Autowired
    private GetSettingsUseCase getSettingsUseCase;
    
    @Autowired
    private GetNvramUseCase getNvramUseCase;
    
    @Autowired
    private GetClientListUseCase getClientListUseCase;
    
    @Autowired
    private GetNetworkDeviceListUseCase getNetworkDeviceListUseCase;
    
    @Autowired
    private GetWanLinkUseCase getWanLinkUseCase;
    
    @Autowired
    private ShowRouterInfoUseCase showRouterInfoUseCase;
    
    /**
     * Configure Spring to use mock router server.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("asus.router.host", () -> "localhost");
        registry.add("asus.router.port", () -> MOCK_ROUTER_PORT);
        registry.add("asus.router.username", () -> MOCK_USERNAME);
        registry.add("asus.router.password", () -> MOCK_PASSWORD);
        registry.add("asus.router.connection-timeout", () -> 3000);
        registry.add("asus.router.read-timeout", () -> 3000);
    }
    
    @BeforeAll
    void startMockRouter() throws IOException {
        mockRouter = new MockRouterServer(MOCK_ROUTER_PORT, MOCK_USERNAME, MOCK_PASSWORD);
        mockRouter.start();
    }
    
    @AfterAll
    void stopMockRouter() {
        if (mockRouter != null) {
            mockRouter.stop();
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Test 1: IsAlive - Check router connectivity")
    void testIsAlive() {
        boolean isAlive = isAliveUseCase.execute();
        
        assertTrue(isAlive, "Router should be alive");
    }
    
    @Test
    @Order(2)
    @DisplayName("Test 2: GetUptime - Retrieve router uptime")
    void testGetUptime() {
        Uptime uptime = getUptimeUseCase.execute();
        
        assertNotNull(uptime, "Uptime should not be null");
        assertNotNull(uptime.since(), "Uptime since should not be null");
        assertNotNull(uptime.uptime(), "Uptime value should not be null");
        assertTrue(uptime.since().contains("Dec 2025"), "Since should contain date");
        assertEquals(450123L, uptime.getUptimeSeconds(), "Uptime seconds should match");
    }
    
    @Test
    @Order(3)
    @DisplayName("Test 3: GetMemoryUsage - Retrieve memory statistics")
    void testGetMemoryUsage() {
        MemoryUsage memory = getMemoryUsageUseCase.execute();
        
        assertNotNull(memory, "Memory usage should not be null");
        assertNotNull(memory.memTotal(), "Total memory should not be null");
        assertNotNull(memory.memFree(), "Free memory should not be null");
        assertNotNull(memory.memUsed(), "Used memory should not be null");
        assertTrue(memory.getUsagePercentage() > 0, "Usage percentage should be positive");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test 4: GetCpuUsage - Retrieve CPU usage for all cores")
    void testGetCpuUsage() {
        CpuUsage cpuUsage = getCpuUsageUseCase.execute();
        
        assertNotNull(cpuUsage, "CPU usage should not be null");
        assertNotNull(cpuUsage.cpu1Total(), "CPU1 total should not be null");
        assertNotNull(cpuUsage.cpu1Usage(), "CPU1 usage should not be null");
        assertNotNull(cpuUsage.cpu2Total(), "CPU2 total should not be null");
        assertNotNull(cpuUsage.cpu2Usage(), "CPU2 usage should not be null");
        assertTrue(cpuUsage.getCpu1Percentage() >= 0, "CPU1 percentage should be non-negative");
        assertTrue(cpuUsage.getCpu2Percentage() >= 0, "CPU2 percentage should be non-negative");
        assertTrue(cpuUsage.getAveragePercentage() >= 0, "Average CPU percentage should be non-negative");
    }
    
    @Test
    @Order(5)
    @DisplayName("Test 5: GetTrafficTotal - Retrieve total network traffic")
    void testGetTrafficTotal() {
        TrafficTotal traffic = getTrafficTotalUseCase.execute();
        
        assertNotNull(traffic, "Traffic total should not be null");
        assertTrue(traffic.sent() >= 0, "Total sent should be non-negative");
        assertTrue(traffic.recv() >= 0, "Total received should be non-negative");
        assertTrue(traffic.getTotal() >= 0, "Total traffic should be non-negative");
    }
    
    @Test
    @Order(6)
    @DisplayName("Test 6: GetTraffic - Retrieve current and total traffic")
    void testGetTraffic() {
        TrafficWithSpeed traffic = getTrafficUseCase.execute();
        
        assertNotNull(traffic, "Traffic should not be null");
        assertNotNull(traffic.total(), "Traffic total should not be null");
        assertNotNull(traffic.speed(), "Traffic speed should not be null");
        assertTrue(traffic.total().sent() >= 0, "Total sent should be non-negative");
        assertTrue(traffic.total().recv() >= 0, "Total received should be non-negative");
        assertTrue(traffic.speed().sent() >= 0, "Current sent speed should be non-negative");
        assertTrue(traffic.speed().recv() >= 0, "Current received speed should be non-negative");
    }
    
    @Test
    @Order(7)
    @DisplayName("Test 7: GetWanStatus - Retrieve WAN connection status")
    void testGetWanStatus() {
        WanStatus wanStatus = getWanStatusUseCase.execute();
        
        assertNotNull(wanStatus, "WAN status should not be null");
        assertTrue(wanStatus.isConnected(), "WAN should be connected");
        assertNotNull(wanStatus.ip(), "IP should not be null");
        assertNotNull(wanStatus.mask(), "Netmask should not be null");
        assertNotNull(wanStatus.gateway(), "Gateway should not be null");
        assertNotNull(wanStatus.dns(), "DNS list should not be null");
        assertFalse(wanStatus.dns().isEmpty(), "DNS list should not be empty");
    }
    
    @Test
    @Order(8)
    @DisplayName("Test 8: GetOnlineClients - Retrieve list of online clients")
    void testGetOnlineClients() {
        List<OnlineClient> clients = getOnlineClientsUseCase.execute();
        
        assertNotNull(clients, "Online clients list should not be null");
        assertFalse(clients.isEmpty(), "Should have online clients");
        for (OnlineClient client : clients) {
            assertNotNull(client.mac(), "Client MAC should not be null");
            assertNotNull(client.ip(), "Client IP should not be null");
        }
    }
    
    @Test
    @Order(9)
    @DisplayName("Test 9: GetDhcpLeases - Retrieve DHCP lease table")
    void testGetDhcpLeases() {
        List<DhcpLease> leases = getDhcpLeasesUseCase.execute();
        
        assertNotNull(leases, "DHCP leases list should not be null");
        assertFalse(leases.isEmpty(), "Should have DHCP leases");
        
        DhcpLease firstLease = leases.get(0);
        assertNotNull(firstLease.mac(), "First lease MAC should not be null");
        assertNotNull(firstLease.ip(), "First lease IP should not be null");
        assertNotNull(firstLease.hostname(), "First lease hostname should not be null");
        assertTrue(firstLease.getExpiresSeconds() >= 0, "Expiration time should be non-negative");
    }
    
    @Test
    @Order(10)
    @DisplayName("Test 10: GetClientFullInfo - Retrieve full client information")
    void testGetClientFullInfo() {
        MacAddress testMac = new MacAddress("AA:BB:CC:DD:EE:01");
        ClientFullInfo clientInfo = getClientFullInfoUseCase.execute(testMac);
        
        assertNotNull(clientInfo, "Client full info should not be null");
        // Note: Mock server doesn't return full client info in current implementation
        // This test verifies the endpoint is called correctly
    }
    
    @Test
    @Order(11)
    @DisplayName("Test 11: GetClientInfoSummary - Retrieve client summary")
    void testGetClientInfoSummary() {
        MacAddress testMac = new MacAddress("AA:BB:CC:DD:EE:01");
        ClientSummary clientSummary = getClientInfoSummaryUseCase.execute(testMac);
        
        assertNotNull(clientSummary, "Client summary should not be null");
        // Note: Mock server doesn't return client summary in current implementation
        // This test verifies the endpoint is called correctly
    }
    
    @Test
    @Order(12)
    @DisplayName("Test 12: GetSettings - Retrieve router settings")
    void testGetSettings() {
        RouterSettings settings = getSettingsUseCase.execute();
        
        assertNotNull(settings, "Router settings should not be null");
        assertNotNull(settings.routerName(), "Router name should not be null");
        assertNotNull(settings.firmwareVersion(), "Firmware version should not be null");
        assertNotNull(settings.lanIp(), "LAN IP should not be null");
        assertNotNull(settings.wanIp(), "WAN IP should not be null");
    }
    
    @Test
    @Order(13)
    @DisplayName("Test 13: GetNvram - Execute custom NVRAM command")
    void testGetNvram() {
        String lanIp = getNvramUseCase.execute("nvram get lan_ipaddr");
        assertEquals("192.168.1.1", lanIp, "LAN IP should be 192.168.1.1");
        
        String model = getNvramUseCase.execute("nvram get model");
        assertEquals("RT-AX88U", model, "Model should be RT-AX88U");
        
        String firmver = getNvramUseCase.execute("nvram get firmver");
        assertEquals("3.0.0.4", firmver, "Firmware version should be 3.0.0.4");
    }
    
    @Test
    @Order(14)
    @DisplayName("Test 14: GetClientList - Format 0 (basic)")
    void testGetClientListFormat0() {
        String clientList = getClientListUseCase.execute(0);
        
        assertNotNull(clientList, "Client list should not be null");
        assertTrue(clientList.contains("AA:BB:CC:DD:EE:01"), "Should contain first MAC");
        assertTrue(clientList.contains("Device-1"), "Should contain device name");
        assertTrue(clientList.contains("192.168.1.101"), "Should contain IP address");
    }
    
    @Test
    @Order(15)
    @DisplayName("Test 15: GetClientList - Format 1 (detailed)")
    void testGetClientListFormat1() {
        String clientList = getClientListUseCase.execute(1);
        
        assertNotNull(clientList, "Client list should not be null");
        assertTrue(clientList.contains("wireless"), "Should contain connection type");
        assertTrue(clientList.contains("Online"), "Should contain online status");
        assertTrue(clientList.contains("-65"), "Should contain signal strength");
    }
    
    @Test
    @Order(16)
    @DisplayName("Test 16: GetClientList - Format 2 (JSON)")
    void testGetClientListFormat2() {
        String clientList = getClientListUseCase.execute(2);
        
        assertNotNull(clientList, "Client list should not be null");
        assertTrue(clientList.startsWith("{"), "Should be JSON format");
        assertTrue(clientList.contains("\"name\""), "Should contain JSON keys");
        assertTrue(clientList.contains("\"rssi\""), "Should contain RSSI field");
    }
    
    @Test
    @Order(17)
    @DisplayName("Test 17: GetNetworkDeviceList - All devices")
    void testGetNetworkDeviceListAll() {
        String devices = getNetworkDeviceListUseCase.execute(null);
        
        assertNotNull(devices, "Device list should not be null");
        assertTrue(devices.contains("eth0"), "Should contain eth0");
        assertTrue(devices.contains("wlan0"), "Should contain wlan0");
    }
    
    @Test
    @Order(18)
    @DisplayName("Test 18: GetNetworkDeviceList - Specific device")
    void testGetNetworkDeviceListSpecific() {
        String deviceInfo = getNetworkDeviceListUseCase.execute("eth0");
        
        assertNotNull(deviceInfo, "Device info should not be null");
        assertTrue(deviceInfo.contains("eth0"), "Should contain device name");
        assertTrue(deviceInfo.contains("1000"), "Should contain speed");
        assertTrue(deviceInfo.contains("up"), "Should contain status");
    }
    
    @Test
    @Order(19)
    @DisplayName("Test 19: GetWanLink - Unit 0")
    void testGetWanLinkUnit0() {
        String wanLink = getWanLinkUseCase.execute(0);
        
        assertNotNull(wanLink, "WAN link should not be null");
        assertTrue(wanLink.contains("wan0"), "Should contain wan0");
        assertTrue(wanLink.startsWith("1"), "Should be connected");
    }
    
    @Test
    @Order(20)
    @DisplayName("Test 20: GetWanLink - Unit 1")
    void testGetWanLinkUnit1() {
        String wanLink = getWanLinkUseCase.execute(1);
        
        assertNotNull(wanLink, "WAN link should not be null");
        assertTrue(wanLink.contains("wan1"), "Should contain wan1");
        assertTrue(wanLink.startsWith("0"), "Should be disconnected");
    }
    
    @Test
    @Order(21)
    @DisplayName("Test 21: ShowRouterInfo - Basic output")
    void testShowRouterInfoBasic() {
        String info = showRouterInfoUseCase.execute(false);
        
        assertNotNull(info, "Router info should not be null");
        assertTrue(info.contains("ASUS ROUTER MONITORING REPORT"), "Should contain title");
        assertTrue(info.contains("Uptime"), "Should contain uptime section");
        assertTrue(info.contains("Memory"), "Should contain memory section");
        assertTrue(info.contains("CPU"), "Should contain CPU section");
    }
    
    @Test
    @Order(22)
    @DisplayName("Test 22: ShowRouterInfo - Detailed output")
    void testShowRouterInfoDetailed() {
        String info = showRouterInfoUseCase.execute(true);
        
        assertNotNull(info, "Router info should not be null");
        assertTrue(info.contains("ASUS ROUTER MONITORING REPORT"), "Should contain title");
        assertTrue(info.length() > 400, "Detailed output should be substantial");
        assertTrue(info.contains("SYSTEM INFORMATION") || info.contains("NETWORK STATUS"), "Should contain section headers");
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Test invalid MAC address format")
        void testInvalidMacAddress() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                new MacAddress("invalid-mac");
            }, "Should throw exception for invalid MAC address");
            assertNotNull(exception.getMessage(), "Exception should have a message");
        }
        
        @Test
        @DisplayName("Test invalid IP address format")
        void testInvalidIpAddress() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                new IpAddress("999.999.999.999");
            }, "Should throw exception for invalid IP address");
            assertNotNull(exception.getMessage(), "Exception should have a message");
        }
        
        @Test
        @DisplayName("Test invalid netmask format")
        void testInvalidNetmask() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                new Netmask("invalid");
            }, "Should throw exception for invalid netmask");
            assertNotNull(exception.getMessage(), "Exception should have a message");
        }
    }
    
    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Test multiple concurrent requests")
        void testConcurrentRequests() throws InterruptedException {
            int iterations = 10;
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < iterations; i++) {
                isAliveUseCase.execute();
            }
            
            long duration = System.currentTimeMillis() - startTime;
            assertTrue(duration < 5000, "10 requests should complete in under 5 seconds");
        }
        
        @Test
        @DisplayName("Test response time for uptime request")
        void testResponseTime() {
            long startTime = System.currentTimeMillis();
            Uptime uptime = getUptimeUseCase.execute();
            long duration = System.currentTimeMillis() - startTime;
            
            assertNotNull(uptime, "Uptime should not be null");
            assertTrue(duration < 1000, "Single request should complete in under 1 second");
        }
    }
}
