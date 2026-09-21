package com.example.realtime.controller;

import com.example.realtime.config.StompPrincipal;
import com.example.realtime.dto.WebSocketMessage;
import com.example.realtime.service.ConnectionManager;
import com.example.realtime.service.ConversationSubscriptionManager;
import com.example.realtime.service.RedisMessagePublisher;
import com.example.realtime.service.WebSocketMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WebSocket Controller
 * Handles incoming WebSocket messages from clients
 * 
 * Message mappings (clients send to /app/*):
 * - /app/conversation.subscribe : Subscribe to a conversation
 * - /app/conversation.unsubscribe : Unsubscribe from a conversation
 * - /app/typing.start : Start typing indicator
 * - /app/typing.stop : Stop typing indicator
 * - /app/ping : Heartbeat ping
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ConversationSubscriptionManager subscriptionManager;
    private final ConnectionManager connectionManager;
    private final RedisMessagePublisher messagePublisher;
    private final WebSocketMessageSender messageSender;

    /**
     * Subscribe to a conversation
     * Client sends: /app/conversation.subscribe
     * Payload: { "conversationId": "conv-123" }
     */
    @MessageMapping("/conversation.subscribe")
    public void subscribeToConversation(@Payload WebSocketMessage message, 
                                       StompHeaderAccessor headerAccessor,
                                       Principal principal) {
        String sessionId = headerAccessor.getSessionId();
        String conversationId = message.getConversationId();
        
        if (principal instanceof StompPrincipal stompPrincipal) {
            String userId = stompPrincipal.getUserId();
            
            log.info("Subscribe request - userId: {}, sessionId: {}, conversationId: {}", 
                    userId, sessionId, conversationId);
            
            try {
                // Register subscription
                subscriptionManager.subscribe(sessionId, userId, conversationId);
                
                // Update heartbeat
                connectionManager.updateHeartbeat(sessionId);
                
                // Send acknowledgment
                WebSocketMessage ack = WebSocketMessage.builder()
                        .type(WebSocketMessage.MessageType.SUBSCRIBED)
                        .eventId(UUID.randomUUID().toString())
                        .conversationId(conversationId)
                        .timestamp(LocalDateTime.now())
                        .build();
                
                messageSender.sendToUser(userId, ack);
                
                log.debug("Subscription successful - userId: {}, conversationId: {}", 
                         userId, conversationId);
                
            } catch (Exception e) {
                log.error("Failed to subscribe - userId: {}, conversationId: {}", 
                         userId, conversationId, e);
                
                messageSender.sendAck(userId, message.getEventId(), false, 
                                    "Failed to subscribe: " + e.getMessage());
            }
        } else {
            log.warn("Subscribe request without valid principal - sessionId: {}", sessionId);
        }
    }

    /**
     * Unsubscribe from a conversation
     * Client sends: /app/conversation.unsubscribe
     * Payload: { "conversationId": "conv-123" }
     */
    @MessageMapping("/conversation.unsubscribe")
    public void unsubscribeFromConversation(@Payload WebSocketMessage message,
                                           StompHeaderAccessor headerAccessor,
                                           Principal principal) {
        String sessionId = headerAccessor.getSessionId();
        String conversationId = message.getConversationId();
        
        if (principal instanceof StompPrincipal stompPrincipal) {
            String userId = stompPrincipal.getUserId();
            
            log.info("Unsubscribe request - userId: {}, sessionId: {}, conversationId: {}", 
                    userId, sessionId, conversationId);
            
            try {
                // Unregister subscription
                subscriptionManager.unsubscribe(sessionId, conversationId);
                
                // Send acknowledgment
                WebSocketMessage ack = WebSocketMessage.builder()
                        .type(WebSocketMessage.MessageType.UNSUBSCRIBED)
                        .eventId(UUID.randomUUID().toString())
                        .conversationId(conversationId)
                        .timestamp(LocalDateTime.now())
                        .build();
                
                messageSender.sendToUser(userId, ack);
                
                log.debug("Unsubscription successful - userId: {}, conversationId: {}", 
                         userId, conversationId);
                
            } catch (Exception e) {
                log.error("Failed to unsubscribe - userId: {}, conversationId: {}", 
                         userId, conversationId, e);
            }
        }
    }

    /**
     * Typing indicator - start
     * Client sends: /app/typing.start
     * Payload: { "conversationId": "conv-123" }
     */
    @MessageMapping("/typing.start")
    public void typingStart(@Payload WebSocketMessage message,
                           Principal principal) {
        if (principal instanceof StompPrincipal stompPrincipal) {
            String userId = stompPrincipal.getUserId();
            String conversationId = message.getConversationId();
            
            log.debug("Typing start - userId: {}, conversationId: {}", userId, conversationId);
            
            // Prepare typing event
            WebSocketMessage typingEvent = WebSocketMessage.builder()
                    .type(WebSocketMessage.MessageType.TYPING_START)
                    .eventId(UUID.randomUUID().toString())
                    .conversationId(conversationId)
                    .senderId(userId)
                    .senderType(stompPrincipal.getUserType())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // Publish to Redis (will be broadcast to all subscribers)
            messagePublisher.publishTypingEvent(conversationId, typingEvent);
        }
    }

    /**
     * Typing indicator - stop
     * Client sends: /app/typing.stop
     * Payload: { "conversationId": "conv-123" }
     */
    @MessageMapping("/typing.stop")
    public void typingStop(@Payload WebSocketMessage message,
                          Principal principal) {
        if (principal instanceof StompPrincipal stompPrincipal) {
            String userId = stompPrincipal.getUserId();
            String conversationId = message.getConversationId();
            
            log.debug("Typing stop - userId: {}, conversationId: {}", userId, conversationId);
            
            // Prepare typing event
            WebSocketMessage typingEvent = WebSocketMessage.builder()
                    .type(WebSocketMessage.MessageType.TYPING_STOP)
                    .eventId(UUID.randomUUID().toString())
                    .conversationId(conversationId)
                    .senderId(userId)
                    .senderType(stompPrincipal.getUserType())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // Publish to Redis
            messagePublisher.publishTypingEvent(conversationId, typingEvent);
        }
    }

    /**
     * Heartbeat ping
     * Client sends: /app/ping
     */
    @MessageMapping("/ping")
    public void ping(StompHeaderAccessor headerAccessor, Principal principal) {
        String sessionId = headerAccessor.getSessionId();
        
        if (principal instanceof StompPrincipal stompPrincipal) {
            String userId = stompPrincipal.getUserId();
            
            // Update heartbeat
            connectionManager.updateHeartbeat(sessionId);
            
            // Send pong response
            WebSocketMessage pong = WebSocketMessage.builder()
                    .type(WebSocketMessage.MessageType.PONG)
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            messageSender.sendToUser(userId, pong);
            
            log.trace("Ping-Pong - userId: {}, sessionId: {}", userId, sessionId);
        }
    }
}
