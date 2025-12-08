package com.asusrouter.infrastructure.adapter.in.mcp;

import com.asusrouter.application.port.in.*;
import com.asusrouter.domain.exception.RouterException;
import com.asusrouter.domain.model.MacAddress;
import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcError;
import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcRequest;
import com.asusrouter.infrastructure.adapter.in.mcp.protocol.JsonRpcResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Handles MCP tool invocations via JSON-RPC 2.0.
 * Routes method calls to appropriate use cases.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class McpJsonRpcHandler {
    
    private final ObjectMapper objectMapper;
    
    // Inject all 17 use cases
    private final GetUptimeUseCase getUptimeUseCase;
    private final GetMemoryUsageUseCase getMemoryUsageUseCase;
    private final GetCpuUsageUseCase getCpuUsageUseCase;
    private final GetTrafficTotalUseCase getTrafficTotalUseCase;
    private final GetTrafficUseCase getTrafficUseCase;
    private final GetWanStatusUseCase getWanStatusUseCase;
    private final GetClientFullInfoUseCase getClientFullInfoUseCase;
    private final GetClientInfoSummaryUseCase getClientInfoSummaryUseCase;
    private final GetOnlineClientsUseCase getOnlineClientsUseCase;
    private final GetDhcpLeasesUseCase getDhcpLeasesUseCase;
    private final GetSettingsUseCase getSettingsUseCase;
    private final GetNvramUseCase getNvramUseCase;
    private final GetClientListUseCase getClientListUseCase;
    private final GetNetworkDeviceListUseCase getNetworkDeviceListUseCase;
    private final GetWanLinkUseCase getWanLinkUseCase;
    private final IsAliveUseCase isAliveUseCase;
    private final ShowRouterInfoUseCase showRouterInfoUseCase;
    
    /**
     * Handle incoming JSON-RPC request.
     */
    public JsonRpcResponse handleRequest(JsonRpcRequest request) {
        try {
            log.debug("Handling MCP request: method={}, id={}", request.getMethod(), request.getId());
            
            if (request.getMethod() == null) {
                return JsonRpcResponse.error(
                    JsonRpcError.invalidRequest("Method is required"),
                    request.getId()
                );
            }
            
            Object result = dispatch(request.getMethod(), request.getParams());
            return JsonRpcResponse.success(result, request.getId());
            
        } catch (RouterException e) {
            log.error("Router error handling request: {}", e.getMessage());
            return JsonRpcResponse.error(
                new JsonRpcError(e.getErrorCode().getCode(), e.getMessage(), e.getErrorCode().name()),
                request.getId()
            );
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters: {}", e.getMessage());
            return JsonRpcResponse.error(
                JsonRpcError.invalidParams(e.getMessage()),
                request.getId()
            );
        } catch (Exception e) {
            log.error("Internal error handling request", e);
            return JsonRpcResponse.error(
                JsonRpcError.internalError(e.getMessage()),
                request.getId()
            );
        }
    }
    
    /**
     * Dispatch method call to appropriate use case.
     */
    private Object dispatch(String method, Object params) {
        JsonNode paramsNode = objectMapper.valueToTree(params);
        
        return switch (method) {
            case "asus_router_get_uptime" -> getUptimeUseCase.execute();
            case "asus_router_get_memory_usage" -> getMemoryUsageUseCase.execute();
            case "asus_router_get_cpu_usage" -> getCpuUsageUseCase.execute();
            case "asus_router_get_traffic_total" -> getTrafficTotalUseCase.execute();
            case "asus_router_get_traffic" -> getTrafficUseCase.execute();
            case "asus_router_get_wan_status" -> getWanStatusUseCase.execute();
            case "asus_router_get_online_clients" -> getOnlineClientsUseCase.execute();
            case "asus_router_get_dhcp_leases" -> getDhcpLeasesUseCase.execute();
            case "asus_router_get_settings" -> getSettingsUseCase.execute();
            case "asus_router_is_alive" -> isAliveUseCase.execute();
            
            case "asus_router_get_client_full_info" -> {
                String mac = paramsNode.path("mac").asText();
                yield getClientFullInfoUseCase.execute(new MacAddress(mac));
            }
            
            case "asus_router_get_client_info_summary" -> {
                String mac = paramsNode.path("mac").asText();
                yield getClientInfoSummaryUseCase.execute(new MacAddress(mac));
            }
            
            case "asus_router_get_nvram" -> {
                String command = paramsNode.path("nvram_command").asText();
                yield getNvramUseCase.execute(command);
            }
            
            case "asus_router_get_client_list" -> {
                Integer format = paramsNode.path("format").isNull() ? null : paramsNode.path("format").asInt();
                yield getClientListUseCase.execute(format);
            }
            
            case "asus_router_get_network_device_list" -> {
                String deviceName = paramsNode.path("device_name").isNull() ? null : paramsNode.path("device_name").asText();
                yield getNetworkDeviceListUseCase.execute(deviceName);
            }
            
            case "asus_router_get_wan_link" -> {
                Integer unit = paramsNode.path("unit").isNull() ? null : paramsNode.path("unit").asInt();
                yield getWanLinkUseCase.execute(unit);
            }
            
            case "asus_router_show_info" -> {
                Boolean detailed = paramsNode.path("detailed").isNull() ? null : paramsNode.path("detailed").asBoolean();
                yield showRouterInfoUseCase.execute(detailed);
            }
            
            case "tools/list" -> listTools();
            
            default -> throw new IllegalArgumentException("Unknown method: " + method);
        };
    }
    
    /**
     * List all available MCP tools.
     */
    private Map<String, Object> listTools() {
        return Map.of(
            "tools", new String[]{
                "asus_router_get_uptime",
                "asus_router_get_memory_usage",
                "asus_router_get_cpu_usage",
                "asus_router_get_traffic_total",
                "asus_router_get_traffic",
                "asus_router_get_wan_status",
                "asus_router_get_client_full_info",
                "asus_router_get_client_info_summary",
                "asus_router_get_online_clients",
                "asus_router_get_dhcp_leases",
                "asus_router_get_settings",
                "asus_router_get_nvram",
                "asus_router_get_client_list",
                "asus_router_get_network_device_list",
                "asus_router_get_wan_link",
                "asus_router_is_alive",
                "asus_router_show_info"
            }
        );
    }
}
