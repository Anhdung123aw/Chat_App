package com.example.realtime.config;

import com.example.realtime.service.ConnectionManager;
import com.example.realtime.service.ConversationSubscriptionManager;
import com.example.realtime.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.security.Principal;

/**
 * WebSocket Event Listener
 * Tracks connection lifecycle events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ConnectionManager connectionManager;
    private final ConversationSubscriptionManager subscriptionManager;
    private final PresenceService presenceService;

    /**
     * Fired when a new WebSocket connection is established
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Principal principal = headerAccessor.getUser();
        
        if (principal instanceof StompPrincipal) {
            StompPrincipal stompPrincipal = (StompPrincipal) principal;
            
            // Register connection
            connectionManager.registerConnection(sessionId, stompPrincipal);
            
            // Update presence to ONLINE
            presenceService.setOnline(stompPrincipal.getUserId(), stompPrincipal.getUserType());
            
            log.info("WebSocket CONNECTED - sessionId: {}, userId: {}", 
                     sessionId, stompPrincipal.getUserId());
        } else {
            log.warn("WebSocket CONNECTED without principal - sessionId: {}", sessionId);
        }
    }

    /**
     * Fired when a client subscribes to a destination
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        log.info("WebSocket SUBSCRIBED - sessionId: {}, destination: {}", sessionId, destination);
        
        // Update heartbeat on subscribe
        connectionManager.updateHeartbeat(sessionId);
    }

    /**
     * Fired when a client unsubscribes from a destination
     */
    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        log.info("WebSocket UNSUBSCRIBED - sessionId: {}", sessionId);
    }

    /**
     * Fired when a WebSocket connection is disconnected
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Get connection info before cleanup
        connectionManager.getConnection(sessionId).ifPresent(connection -> {
            String userId = connection.getUserId();
            
            // Cleanup subscriptions
            subscriptionManager.unsubscribeAll(sessionId);
            
            // Unregister connection
            connectionManager.unregisterConnection(sessionId);
            
            // Update presence to OFFLINE (only if user has no other active sessions)
            if (!connectionManager.isUserOnline(userId)) {
                presenceService.setOffline(userId);
            }
            
            log.info("WebSocket DISCONNECTED - sessionId: {}, userId: {}", sessionId, userId);
        });
        
        // Fallback if connection not found
        if (!connectionManager.getConnection(sessionId).isPresent()) {
            subscriptionManager.unsubscribeAll(sessionId);
            connectionManager.unregisterConnection(sessionId);
            log.info("WebSocket DISCONNECTED - sessionId: {} (connection not found)", sessionId);
        }
    }
}
