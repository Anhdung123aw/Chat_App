package com.example.realtime.service;

import com.example.realtime.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis Message Publisher
 * Publishes messages to Redis channels for distribution across service instances
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${chat.realtime.redis.message-channel-prefix:chat:message:}")
    private String messageChannelPrefix;

    @Value("${chat.realtime.redis.typing-channel-prefix:chat:typing:}")
    private String typingChannelPrefix;

    @Value("${chat.realtime.redis.presence-channel-prefix:chat:presence:}")
    private String presenceChannelPrefix;

    /**
     * Publish a message to a conversation channel
     * All instances subscribed to this conversation will receive it
     */
    public void publishMessage(String conversationId, WebSocketMessage message) {
        String channel = messageChannelPrefix + conversationId;
        
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("Published message to Redis channel: {} - messageId: {}", 
                     channel, message.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish message to Redis channel: {}", channel, e);
        }
    }

    /**
     * Publish typing indicator event
     */
    public void publishTypingEvent(String conversationId, WebSocketMessage message) {
        String channel = typingChannelPrefix + conversationId;
        
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("Published typing event to Redis channel: {}", channel);
        } catch (Exception e) {
            log.error("Failed to publish typing event to Redis channel: {}", channel, e);
        }
    }

    /**
     * Publish presence update event
     */
    public void publishPresenceEvent(String userId, WebSocketMessage message) {
        String channel = presenceChannelPrefix + userId;
        
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("Published presence event to Redis channel: {}", channel);
        } catch (Exception e) {
            log.error("Failed to publish presence event to Redis channel: {}", channel, e);
        }
    }

    /**
     * Publish to a custom channel
     */
    public void publish(String channel, Object message) {
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("Published to Redis channel: {}", channel);
        } catch (Exception e) {
            log.error("Failed to publish to Redis channel: {}", channel, e);
        }
    }
}
