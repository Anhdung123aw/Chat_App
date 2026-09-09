package com.example.chatrealtime.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketSessionManager {

    // Map: conversationId -> Set of WebSocketSessions
    private final Map<String, Set<WebSocketSession>> conversationSessions = new ConcurrentHashMap<>();

    // Map: userId -> Set of WebSocketSessions
    private final Map<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void addSessionToUser(String userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("User {} connected. Session: {}", userId, session.getId());
    }

    public void removeSession(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
        
        // Remove from all conversations
        conversationSessions.values().forEach(sessions -> sessions.remove(session));
        log.info("Session {} removed.", session.getId());
    }

    public void subscribeConversation(String conversationId, WebSocketSession session) {
        conversationSessions.computeIfAbsent(conversationId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("Session {} subscribed to conversation {}", session.getId(), conversationId);
    }

    public void broadcastToConversation(String conversationId, String messageJson) {
        Set<WebSocketSession> sessions = conversationSessions.get(conversationId);
        if (sessions != null && !sessions.isEmpty()) {
            TextMessage textMessage = new TextMessage(messageJson);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("Failed to send message to session {}", session.getId(), e);
                    }
                }
            }
        }
    }
}
