package com.example.realtime.service;

import com.example.realtime.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * WebSocket Message Sender
 * Sends messages to WebSocket clients via STOMP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageSender {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConnectionManager connectionManager;

    /**
     * Send message to a specific user (all their sessions)
     * Uses /user destination prefix
     */
    public void sendToUser(String userId, WebSocketMessage message) {
        try {
            // Check if user is online (has active sessions)
            if (connectionManager.isUserOnline(userId)) {
                messagingTemplate.convertAndSendToUser(
                        userId,
                        "/queue/messages",
                        message
                );
                log.debug("Sent message to user: {} - type: {}", userId, message.getType());
            } else {
                log.debug("User not online, skipping send: {}", userId);
            }
        } catch (Exception e) {
            log.error("Error sending message to user: {}", userId, e);
        }
    }

    /**
     * Send message to all subscribers of a conversation
     * Uses /topic destination
     */
    public void sendToConversation(String conversationId, WebSocketMessage message) {
        try {
            String destination = "/topic/conversation/" + conversationId;
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Sent message to conversation: {} - type: {}", conversationId, message.getType());
        } catch (Exception e) {
            log.error("Error sending message to conversation: {}", conversationId, e);
        }
    }

    /**
     * Send message to a specific session
     */
    public void sendToSession(String sessionId, WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSend("/queue/messages-" + sessionId, message);
            log.debug("Sent message to session: {} - type: {}", sessionId, message.getType());
        } catch (Exception e) {
            log.error("Error sending message to session: {}", sessionId, e);
        }
    }

    /**
     * Broadcast message to all connected clients
     */
    public void broadcast(WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSend("/topic/broadcast", message);
            log.debug("Broadcast message - type: {}", message.getType());
        } catch (Exception e) {
            log.error("Error broadcasting message", e);
        }
    }

    /**
     * Send message to multiple users
     */
    public void sendToUsers(Set<String> userIds, WebSocketMessage message) {
        userIds.forEach(userId -> sendToUser(userId, message));
    }

    /**
     * Send acknowledgment message
     */
    public void sendAck(String userId, String eventId, boolean success, String errorMessage) {
        WebSocketMessage ackMessage = WebSocketMessage.builder()
                .type(success ? WebSocketMessage.MessageType.MESSAGE_ACK : WebSocketMessage.MessageType.ERROR)
                .eventId(eventId)
                .errorMessage(errorMessage)
                .build();
        
        sendToUser(userId, ackMessage);
    }
}
