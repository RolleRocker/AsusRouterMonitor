package com.asusrouter.infrastructure.adapter.in.mcp;

import com.asusrouter.application.port.in.GetUptimeUseCase;
import com.asusrouter.application.port.in.IsAliveUseCase;
import com.asusrouter.domain.model.Uptime;
import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcError;
import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcRequest;
import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for McpJsonRpcHandler.
 */
@ExtendWith(MockitoExtension.class)
class McpJsonRpcHandlerTest {
    
    @Mock
    private GetUptimeUseCase getUptimeUseCase;
    
    @Mock
    private IsAliveUseCase isAliveUseCase;
    
    private McpJsonRpcHandler handler;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Create handler with only the mocked use cases we need for tests
        handler = new McpJsonRpcHandler(
            objectMapper,
            getUptimeUseCase,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            isAliveUseCase,
            null
        );
    }
    
    @Test
    void shouldHandleGetUptimeRequest() {
        // Given
        Uptime uptime = new Uptime("Thu, 22 Jul 2021 14:32:38 +0200", "375001");
        when(getUptimeUseCase.execute()).thenReturn(uptime);
        
        JsonRpcRequest request = new JsonRpcRequest("2.0", "asus_router_get_uptime", null, 1);
        
        // When
        JsonRpcResponse response = handler.handleRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals("2.0", response.getJsonrpc());
        assertEquals(1, response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
        
        verify(getUptimeUseCase).execute();
    }
    
    @Test
    void shouldHandleIsAliveRequest() {
        // Given
        when(isAliveUseCase.execute()).thenReturn(true);
        
        JsonRpcRequest request = new JsonRpcRequest("2.0", "asus_router_is_alive", null, 2);
        
        // When
        JsonRpcResponse response = handler.handleRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals(2, response.getId());
        assertNull(response.getError());
        assertEquals(true, response.getResult());
        
        verify(isAliveUseCase).execute();
    }
    
    @Test
    void shouldReturnErrorForUnknownMethod() {
        // Given
        JsonRpcRequest request = new JsonRpcRequest("2.0", "unknown_method", null, 3);
        
        // When
        JsonRpcResponse response = handler.handleRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals(3, response.getId());
        assertNull(response.getResult());
        assertNotNull(response.getError());
        assertEquals(JsonRpcError.INVALID_PARAMS, response.getError().getCode());
    }
    
    @Test
    void shouldReturnErrorForNullMethod() {
        // Given
        JsonRpcRequest request = new JsonRpcRequest("2.0", null, null, 4);
        
        // When
        JsonRpcResponse response = handler.handleRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals(4, response.getId());
        assertNull(response.getResult());
        assertNotNull(response.getError());
        assertEquals(JsonRpcError.INVALID_REQUEST, response.getError().getCode());
    }
    
    @Test
    void shouldHandleListToolsRequest() {
        // Given
        JsonRpcRequest request = new JsonRpcRequest("2.0", "tools/list", null, 5);
        
        // When
        JsonRpcResponse response = handler.handleRequest(request);
        
        // Then
        assertNotNull(response);
        assertEquals(5, response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
    }
}
