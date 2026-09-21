package com.example.realtime.service;

import com.example.realtime.dto.WebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

/**
 * Redis Message Subscriber
 * Listens to Redis channels and forwards messages to WebSocket clients
 */
@Slf4j
@Service
public class RedisMessageSubscriber implements MessageListener {

    private final WebSocketMessageSender messageSender;
    private final ObjectMapper objectMapper;

    public RedisMessageSubscriber(WebSocketMessageSender messageSender,
                                  @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
    }

    /**
     * Called when a message is received from Redis Pub/Sub
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            byte[] body = message.getBody();
            
            // Deserialize message
            WebSocketMessage wsMessage = objectMapper.readValue(body, WebSocketMessage.class);
            
            log.debug("Received message from Redis channel: {} - type: {}", 
                     channel, wsMessage.getType());
            
            // Route message based on channel pattern
            if (channel.startsWith("chat:message:")) {
                handleMessageEvent(channel, wsMessage);
            } else if (channel.startsWith("chat:typing:")) {
                handleTypingEvent(channel, wsMessage);
            } else if (channel.startsWith("chat:presence:")) {
                handlePresenceEvent(channel, wsMessage);
            } else {
                log.warn("Unknown Redis channel pattern: {}", channel);
            }
            
        } catch (Exception e) {
            log.error("Error processing Redis message", e);
        }
    }

    /**
     * Handle message events (new message in conversation)
     */
    private void handleMessageEvent(String channel, WebSocketMessage message) {
        String conversationId = message.getConversationId();
        if (conversationId != null) {
            // Broadcast to all subscribers of this conversation
            messageSender.sendToConversation(conversationId, message);
            log.debug("Forwarded message to conversation: {}", conversationId);
        }
    }

    /**
     * Handle typing indicator events
     */
    private void handleTypingEvent(String channel, WebSocketMessage message) {
        String conversationId = message.getConversationId();
        if (conversationId != null) {
            // Broadcast typing indicator to conversation
            messageSender.sendToConversation(conversationId, message);
            log.debug("Forwarded typing event to conversation: {}", conversationId);
        }
    }

    /**
     * Handle presence events (user online/offline)
     */
    private void handlePresenceEvent(String channel, WebSocketMessage message) {
        String userId = message.getSenderId();
        if (userId != null) {
            // Broadcast presence update to interested parties
            messageSender.sendToUser(userId, message);
            log.debug("Forwarded presence event for user: {}", userId);
        }
    }
}
