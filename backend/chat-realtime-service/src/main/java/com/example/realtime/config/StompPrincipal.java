package com.example.realtime.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.security.Principal;

/**
 * STOMP Principal - represents authenticated user in WebSocket session
 * 
 * For development: userId is passed as query parameter or header
 * For production: userId is extracted from JWT token (Task #10)
 */
@Data
@AllArgsConstructor
public class StompPrincipal implements Principal {
    
    private String userId;
    private String userType; // USER, AGENT, SYSTEM
    
    @Override
    public String getName() {
        return userId;
    }
}
