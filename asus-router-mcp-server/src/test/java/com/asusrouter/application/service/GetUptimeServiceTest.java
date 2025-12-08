package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetUptimeUseCase;
import com.asusrouter.application.port.out.RouterUptimePort;
import com.asusrouter.domain.model.Uptime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for GetUptimeService.
 */
@ExtendWith(MockitoExtension.class)
class GetUptimeServiceTest {
    
    @Mock
    private RouterUptimePort routerUptimePort;
    
    private GetUptimeService service;
    
    @BeforeEach
    void setUp() {
        service = new GetUptimeService(routerUptimePort);
    }
    
    @Test
    void shouldParseValidUptimeResponse() {
        // Given
        String routerResponse = "Thu, 22 Jul 2021 14:32:38 +0200;375001";
        when(routerUptimePort.getUptime()).thenReturn(routerResponse);
        
        // When
        Uptime result = service.execute();
        
        // Then
        assertNotNull(result);
        assertEquals("Thu, 22 Jul 2021 14:32:38 +0200", result.since());
        assertEquals("375001", result.uptime());
        assertEquals(375001L, result.getUptimeSeconds());
        
        verify(routerUptimePort).getUptime();
    }
    
    @Test
    void shouldThrowExceptionForInvalidFormat() {
        // Given
        String invalidResponse = "invalid response";
        when(routerUptimePort.getUptime()).thenReturn(invalidResponse);
        
        // When/Then
        assertThrows(IllegalStateException.class, () -> service.execute());
    }
    
    @Test
    void shouldThrowExceptionForMissingSeparator() {
        // Given
        String invalidResponse = "Thu, 22 Jul 2021 14:32:38 +0200";
        when(routerUptimePort.getUptime()).thenReturn(invalidResponse);
        
        // When/Then
        assertThrows(IllegalStateException.class, () -> service.execute());
    }
}
