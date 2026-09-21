package com.example.realtime.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Conversation Subscription Manager
 * Manages which sessions are subscribed to which conversations
 * 
 * This allows:
 * - Track who is listening to a conversation
 * - Route messages only to subscribed sessions
 * - Cleanup subscriptions on disconnect
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationSubscriptionManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConnectionManager connectionManager;

    // Local cache: conversationId -> Set<sessionId>
    private final Map<String, Set<String>> conversationSubscribers = new ConcurrentHashMap<>();
    
    // Local cache: sessionId -> Set<conversationId>
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();

    private static final String REDIS_SUBSCRIPTION_KEY_PREFIX = "chat:subscription:";
    private static final long SUBSCRIPTION_TTL_SECONDS = 300; // 5 minutes

    /**
     * Subscribe a session to a conversation
     */
    public void subscribe(String sessionId, String userId, String conversationId) {
        // Add to local cache
        conversationSubscribers
                .computeIfAbsent(conversationId, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
        
        sessionSubscriptions
                .computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet())
                .add(conversationId);
        
        // Store in Redis (for cross-instance awareness)
        String redisKey = REDIS_SUBSCRIPTION_KEY_PREFIX + conversationId;
        redisTemplate.opsForSet().add(redisKey, sessionId);
        redisTemplate.expire(redisKey, SUBSCRIPTION_TTL_SECONDS, TimeUnit.SECONDS);
        
        // Update connection info
        connectionManager.getConnection(sessionId).ifPresent(connection -> {
            connection.getSubscribedConversations().add(conversationId);
        });
        
        log.info("Subscribed - sessionId: {}, userId: {}, conversationId: {}", 
                sessionId, userId, conversationId);
    }

    /**
     * Unsubscribe a session from a conversation
     */
    public void unsubscribe(String sessionId, String conversationId) {
        // Remove from local cache
        Set<String> subscribers = conversationSubscribers.get(conversationId);
        if (subscribers != null) {
            subscribers.remove(sessionId);
            if (subscribers.isEmpty()) {
                conversationSubscribers.remove(conversationId);
            }
        }
        
        Set<String> subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions != null) {
            subscriptions.remove(conversationId);
            if (subscriptions.isEmpty()) {
                sessionSubscriptions.remove(sessionId);
            }
        }
        
        // Remove from Redis
        String redisKey = REDIS_SUBSCRIPTION_KEY_PREFIX + conversationId;
        redisTemplate.opsForSet().remove(redisKey, sessionId);
        
        // Update connection info
        connectionManager.getConnection(sessionId).ifPresent(connection -> {
            connection.getSubscribedConversations().remove(conversationId);
        });
        
        log.info("Unsubscribed - sessionId: {}, conversationId: {}", sessionId, conversationId);
    }

    /**
     * Unsubscribe a session from all conversations (on disconnect)
     */
    public void unsubscribeAll(String sessionId) {
        Set<String> subscriptions = sessionSubscriptions.remove(sessionId);
        
        if (subscriptions != null && !subscriptions.isEmpty()) {
            log.info("Unsubscribing session from {} conversations - sessionId: {}", 
                    subscriptions.size(), sessionId);
            
            for (String conversationId : subscriptions) {
                Set<String> subscribers = conversationSubscribers.get(conversationId);
                if (subscribers != null) {
                    subscribers.remove(sessionId);
                    if (subscribers.isEmpty()) {
                        conversationSubscribers.remove(conversationId);
                    }
                }
                
                // Remove from Redis
                String redisKey = REDIS_SUBSCRIPTION_KEY_PREFIX + conversationId;
                redisTemplate.opsForSet().remove(redisKey, sessionId);
            }
        }
    }

    /**
     * Get all sessions subscribed to a conversation (local only)
     */
    public Set<String> getSubscribers(String conversationId) {
        Set<String> subscribers = conversationSubscribers.get(conversationId);
        return subscribers != null ? new HashSet<>(subscribers) : Collections.emptySet();
    }

    /**
     * Get all conversations a session is subscribed to
     */
    public Set<String> getSubscriptions(String sessionId) {
        Set<String> subscriptions = sessionSubscriptions.get(sessionId);
        return subscriptions != null ? new HashSet<>(subscriptions) : Collections.emptySet();
    }

    /**
     * Get all subscribers from Redis (cross-instance)
     */
    public Set<String> getSubscribersFromRedis(String conversationId) {
        String redisKey = REDIS_SUBSCRIPTION_KEY_PREFIX + conversationId;
        Set<Object> redisSet = redisTemplate.opsForSet().members(redisKey);
        
        if (redisSet != null && !redisSet.isEmpty()) {
            Set<String> result = new HashSet<>();
            redisSet.forEach(obj -> result.add((String) obj));
            return result;
        }
        
        return Collections.emptySet();
    }

    /**
     * Check if a session is subscribed to a conversation
     */
    public boolean isSubscribed(String sessionId, String conversationId) {
        Set<String> subscribers = conversationSubscribers.get(conversationId);
        return subscribers != null && subscribers.contains(sessionId);
    }

    /**
     * Get total number of local subscriptions
     */
    public int getLocalSubscriptionCount() {
        return sessionSubscriptions.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * Get subscriber count for a conversation (local only)
     */
    public int getSubscriberCount(String conversationId) {
        Set<String> subscribers = conversationSubscribers.get(conversationId);
        return subscribers != null ? subscribers.size() : 0;
    }
}
