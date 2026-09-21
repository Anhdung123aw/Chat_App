package com.example.realtime.service;

import com.example.realtime.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Presence Service
 * Tracks user online/offline/away status
 * 
 * Presence Status:
 * - ONLINE: User is connected and active
 * - AWAY: User is connected but inactive
 * - OFFLINE: User is disconnected
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessagePublisher messagePublisher;
    private final ConnectionManager connectionManager;

    @Value("${chat.realtime.presence-ttl-seconds:300}")
    private int presenceTtlSeconds;

    private static final String REDIS_PRESENCE_KEY_PREFIX = "chat:presence:";
    private static final String REDIS_PRESENCE_TIMESTAMP_KEY_PREFIX = "chat:presence:timestamp:";

    /**
     * Presence status enum
     */
    public enum PresenceStatus {
        ONLINE,
        AWAY,
        OFFLINE
    }

    /**
     * Update user presence status
     */
    public void updatePresence(String userId, PresenceStatus status) {
        try {
            // Store in Redis
            String presenceKey = REDIS_PRESENCE_KEY_PREFIX + userId;
            redisTemplate.opsForValue().set(presenceKey, status.name(), presenceTtlSeconds, TimeUnit.SECONDS);
            
            // Store timestamp
            String timestampKey = REDIS_PRESENCE_TIMESTAMP_KEY_PREFIX + userId;
            redisTemplate.opsForValue().set(timestampKey, LocalDateTime.now(), presenceTtlSeconds, TimeUnit.SECONDS);
            
            // Publish presence event
            publishPresenceEvent(userId, status);
            
            log.debug("Updated presence - userId: {}, status: {}", userId, status);
            
        } catch (Exception e) {
            log.error("Failed to update presence - userId: {}, status: {}", userId, status, e);
        }
    }

    /**
     * Set user online
     */
    public void setOnline(String userId, String userType) {
        updatePresence(userId, PresenceStatus.ONLINE);
        log.info("User ONLINE - userId: {}, userType: {}", userId, userType);
    }

    /**
     * Set user away
     */
    public void setAway(String userId) {
        updatePresence(userId, PresenceStatus.AWAY);
        log.info("User AWAY - userId: {}", userId);
    }

    /**
     * Set user offline
     */
    public void setOffline(String userId) {
        updatePresence(userId, PresenceStatus.OFFLINE);
        log.info("User OFFLINE - userId: {}", userId);
    }

    /**
     * Get user presence status
     */
    public PresenceStatus getPresence(String userId) {
        try {
            String presenceKey = REDIS_PRESENCE_KEY_PREFIX + userId;
            Object status = redisTemplate.opsForValue().get(presenceKey);
            
            if (status != null) {
                return PresenceStatus.valueOf(status.toString());
            }
            
            // Check if user has active connections
            if (connectionManager.isUserOnline(userId)) {
                return PresenceStatus.ONLINE;
            }
            
            return PresenceStatus.OFFLINE;
            
        } catch (Exception e) {
            log.error("Failed to get presence - userId: {}", userId, e);
            return PresenceStatus.OFFLINE;
        }
    }

    /**
     * Get user last seen timestamp
     */
    public LocalDateTime getLastSeen(String userId) {
        try {
            String timestampKey = REDIS_PRESENCE_TIMESTAMP_KEY_PREFIX + userId;
            Object timestamp = redisTemplate.opsForValue().get(timestampKey);
            
            if (timestamp instanceof LocalDateTime) {
                return (LocalDateTime) timestamp;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Failed to get last seen - userId: {}", userId, e);
            return null;
        }
    }

    /**
     * Check if user is online
     */
    public boolean isOnline(String userId) {
        PresenceStatus status = getPresence(userId);
        return status == PresenceStatus.ONLINE || status == PresenceStatus.AWAY;
    }

    /**
     * Publish presence event to Redis
     */
    private void publishPresenceEvent(String userId, PresenceStatus status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("status", status.name());
        payload.put("timestamp", LocalDateTime.now());
        
        WebSocketMessage presenceEvent = WebSocketMessage.builder()
                .type(determineMessageType(status))
                .eventId(UUID.randomUUID().toString())
                .senderId(userId)
                .timestamp(LocalDateTime.now())
                .payload(payload)
                .build();
        
        messagePublisher.publishPresenceEvent(userId, presenceEvent);
    }

    /**
     * Determine WebSocket message type based on presence status
     */
    private WebSocketMessage.MessageType determineMessageType(PresenceStatus status) {
        return switch (status) {
            case ONLINE -> WebSocketMessage.MessageType.USER_ONLINE;
            case OFFLINE -> WebSocketMessage.MessageType.USER_OFFLINE;
            case AWAY -> WebSocketMessage.MessageType.PRESENCE_UPDATE;
        };
    }

    /**
     * Bulk get presence for multiple users
     */
    public Map<String, PresenceStatus> getBulkPresence(Iterable<String> userIds) {
        Map<String, PresenceStatus> result = new HashMap<>();
        
        for (String userId : userIds) {
            result.put(userId, getPresence(userId));
        }
        
        return result;
    }
}
