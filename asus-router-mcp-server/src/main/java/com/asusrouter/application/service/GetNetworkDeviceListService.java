package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetNetworkDeviceListUseCase;
import com.asusrouter.application.port.out.RouterNetworkDevicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case implementation for retrieving network device list.
 */
@Service
@RequiredArgsConstructor
public class GetNetworkDeviceListService implements GetNetworkDeviceListUseCase {
    
    private final RouterNetworkDevicePort routerNetworkDevicePort;
    
    @Override
    public String execute(String deviceName) {
        // Return raw JSON response directly
        // Device name can filter results (e.g., "eth0", "wl0")
        return routerNetworkDevicePort.getNetworkDeviceList(deviceName);
    }
}
