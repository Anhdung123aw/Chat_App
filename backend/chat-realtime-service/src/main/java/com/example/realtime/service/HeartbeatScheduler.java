package com.example.realtime.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Heartbeat Scheduler - cleanup stale connections
 * 
 * Runs periodically to check for connections without heartbeat
 * and removes them from the connection manager
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HeartbeatScheduler {

    private final ConnectionManager connectionManager;

    @Value("${chat.realtime.connection-timeout-seconds:300}")
    private int connectionTimeoutSeconds;

    /**
     * Check for stale connections every 60 seconds
     */
    @Scheduled(fixedRate = 60000) // 1 minute
    public void cleanupStaleConnections() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(connectionTimeoutSeconds);
        List<String> staleSessionIds = new ArrayList<>();
        
        for (ConnectionManager.ConnectionInfo connection : connectionManager.getLocalSessions()) {
            if (connection.getLastHeartbeat().isBefore(cutoffTime)) {
                staleSessionIds.add(connection.getSessionId());
            }
        }
        
        if (!staleSessionIds.isEmpty()) {
            log.info("Cleaning up {} stale connections (timeout: {}s)", 
                     staleSessionIds.size(), connectionTimeoutSeconds);
            
            staleSessionIds.forEach(sessionId -> {
                log.debug("Removing stale connection: {}", sessionId);
                connectionManager.unregisterConnection(sessionId);
            });
        }
    }

    /**
     * Log connection statistics every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void logConnectionStats() {
        int totalConnections = connectionManager.getLocalConnectionCount();
        log.info("Active WebSocket connections on this instance: {}", totalConnections);
    }
}
