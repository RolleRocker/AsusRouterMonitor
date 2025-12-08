package com.asusrouter.application.service;

import com.asusrouter.application.port.in.GetNvramUseCase;
import com.asusrouter.application.port.out.RouterNvramPort;
import com.asusrouter.domain.exception.ErrorCode;
import com.asusrouter.domain.exception.RouterException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case implementation for executing custom NVRAM commands.
 */
@Service
@RequiredArgsConstructor
public class GetNvramService implements GetNvramUseCase {
    
    private final RouterNvramPort routerNvramPort;
    
    @Override
    public String execute(String nvramCommand) {
        validateCommand(nvramCommand);
        return routerNvramPort.executeNvramCommand(nvramCommand);
    }
    
    /**
     * Validate NVRAM command for basic safety.
     * Prevents potentially dangerous commands.
     */
    private void validateCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            throw new RouterException(
                ErrorCode.INVALID_COMMAND,
                "NVRAM command cannot be null or empty"
            );
        }
        
        // Remove multiple spaces and trim
        String normalized = command.trim().replaceAll("\\s+", " ");
        
        // Basic validation - should start with nvram
        if (!normalized.toLowerCase().startsWith("nvram")) {
            throw new RouterException(
                ErrorCode.INVALID_COMMAND,
                "Command must start with 'nvram'"
            );
        }
        
        // Warn about potentially dangerous operations
        String lower = normalized.toLowerCase();
        if (lower.contains("set") || lower.contains("commit") || lower.contains("erase")) {
            throw new RouterException(
                ErrorCode.INVALID_COMMAND,
                "Write operations (set/commit/erase) are not allowed for safety"
            );
        }
    }
}
