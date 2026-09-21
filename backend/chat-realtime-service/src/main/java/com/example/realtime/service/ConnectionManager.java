package com.example.realtime.service;

import com.example.realtime.config.StompPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Connection Manager - manages WebSocket connections
 * 
 * Responsibilities:
 * - Track active WebSocket sessions
 * - Map userId -> sessionIds (1 user can have multiple sessions)
 * - Store connection metadata in Redis (for multi-instance support)
 * - Handle connection registration/unregistration
 * - Provide session lookup methods
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionManager {

    private final RedisTemplate<String, Object> redisTemplate;

    // Local cache: sessionId -> ConnectionInfo
    private final Map<String, ConnectionInfo> localSessions = new ConcurrentHashMap<>();
    
    // Local cache: userId -> Set<sessionId>
    private final Map<String, Set<String>> userToSessions = new ConcurrentHashMap<>();

    private static final String REDIS_CONNECTION_KEY_PREFIX = "chat:connection:";
    private static final String REDIS_USER_SESSIONS_KEY_PREFIX = "chat:user:sessions:";
    private static final long CONNECTION_TTL_SECONDS = 300; // 5 minutes

    /**
     * Register a new WebSocket connection
     */
    public void registerConnection(String sessionId, StompPrincipal principal) {
        String userId = principal.getUserId();
        String userType = principal.getUserType();
        
        ConnectionInfo connectionInfo = ConnectionInfo.builder()
                .sessionId(sessionId)
                .userId(userId)
                .userType(userType)
                .connectedAt(LocalDateTime.now())
                .lastHeartbeat(LocalDateTime.now())
                .build();
        
        // Store in local cache
        localSessions.put(sessionId, connectionInfo);
        userToSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        
        // Store in Redis (for multi-instance support)
        String redisKey = REDIS_CONNECTION_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(redisKey, connectionInfo, CONNECTION_TTL_SECONDS, TimeUnit.SECONDS);
        
        // Add to user's session set in Redis
        String userSessionsKey = REDIS_USER_SESSIONS_KEY_PREFIX + userId;
        redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        redisTemplate.expire(userSessionsKey, CONNECTION_TTL_SECONDS, TimeUnit.SECONDS);
        
        log.info("Connection registered - sessionId: {}, userId: {}, userType: {}", 
                 sessionId, userId, userType);
    }

    /**
     * Unregister a WebSocket connection
     */
    public void unregisterConnection(String sessionId) {
        ConnectionInfo connectionInfo = localSessions.remove(sessionId);
        
        if (connectionInfo != null) {
            String userId = connectionInfo.getUserId();
            
            // Remove from user's session set
            Set<String> sessions = userToSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userToSessions.remove(userId);
                }
            }
            
            // Remove from Redis
            redisTemplate.delete(REDIS_CONNECTION_KEY_PREFIX + sessionId);
            redisTemplate.opsForSet().remove(REDIS_USER_SESSIONS_KEY_PREFIX + userId, sessionId);
            
            log.info("Connection unregistered - sessionId: {}, userId: {}", sessionId, userId);
        } else {
            log.warn("Attempted to unregister unknown sessionId: {}", sessionId);
        }
    }

    /**
     * Update heartbeat timestamp for a session
     */
    public void updateHeartbeat(String sessionId) {
        ConnectionInfo connectionInfo = localSessions.get(sessionId);
        if (connectionInfo != null) {
            connectionInfo.setLastHeartbeat(LocalDateTime.now());
            
            // Refresh TTL in Redis
            String redisKey = REDIS_CONNECTION_KEY_PREFIX + sessionId;
            redisTemplate.expire(redisKey, CONNECTION_TTL_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * Get connection info by sessionId
     */
    public Optional<ConnectionInfo> getConnection(String sessionId) {
        ConnectionInfo connectionInfo = localSessions.get(sessionId);
        if (connectionInfo != null) {
            return Optional.of(connectionInfo);
        }
        
        // Fallback to Redis (in case this instance doesn't have the session)
        String redisKey = REDIS_CONNECTION_KEY_PREFIX + sessionId;
        Object obj = redisTemplate.opsForValue().get(redisKey);
        return Optional.ofNullable((ConnectionInfo) obj);
    }

    /**
     * Get all active session IDs for a user
     */
    public Set<String> getUserSessions(String userId) {
        Set<String> sessions = userToSessions.get(userId);
        if (sessions != null && !sessions.isEmpty()) {
            return new HashSet<>(sessions);
        }
        
        // Fallback to Redis
        String userSessionsKey = REDIS_USER_SESSIONS_KEY_PREFIX + userId;
        Set<Object> redisSet = redisTemplate.opsForSet().members(userSessionsKey);
        if (redisSet != null && !redisSet.isEmpty()) {
            Set<String> result = new HashSet<>();
            redisSet.forEach(obj -> result.add((String) obj));
            return result;
        }
        
        return Collections.emptySet();
    }

    /**
     * Check if user is online (has at least one active session)
     */
    public boolean isUserOnline(String userId) {
        return !getUserSessions(userId).isEmpty();
    }

    /**
     * Get all local active sessions (on this instance)
     */
    public Collection<ConnectionInfo> getLocalSessions() {
        return localSessions.values();
    }

    /**
     * Get total count of local connections
     */
    public int getLocalConnectionCount() {
        return localSessions.size();
    }

    /**
     * Get count of connections for a specific user (local only)
     */
    public int getUserConnectionCount(String userId) {
        Set<String> sessions = userToSessions.get(userId);
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * Connection Information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConnectionInfo {
        private String sessionId;
        private String userId;
        private String userType; // USER, AGENT, SYSTEM
        private LocalDateTime connectedAt;
        private LocalDateTime lastHeartbeat;
        
        @lombok.Builder.Default
        private Set<String> subscribedConversations = new HashSet<>();
    }
}
