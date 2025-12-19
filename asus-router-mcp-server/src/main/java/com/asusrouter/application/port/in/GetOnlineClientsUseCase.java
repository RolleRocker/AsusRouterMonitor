package com.asusrouter.application.port.in;


import com.asusrouter.domain.model.OnlineClient;
import com.asusrouter.mcp.annotations.McpTool;
import java.util.List;

/**
 * Port for retrieving list of online clients.
 */
@McpTool(
    name = "asus_router_get_online_clients",
    description = "Retrieve list of currently online/connected clients with their basic information",
    errorCodes = {"ROUTER_AUTH_FAILED", "ROUTER_COMM_ERROR"}
)
public interface GetOnlineClientsUseCase {
    /**
     * Execute the use case to get online clients.
     * @return List of online clients
     */
    List<OnlineClient> execute();
}
