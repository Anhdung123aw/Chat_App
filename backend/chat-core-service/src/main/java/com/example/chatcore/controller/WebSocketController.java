package com.example.chatcore.controller;

import com.example.chatcore.dto.MessageDTO.*;
import com.example.chatcore.entity.ChatMessageEntity;
import com.example.chatcore.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket Controller
 * Handles real-time messaging via WebSocket/STOMP
 * 
 * Client connects to: ws://localhost:8080/ws
 * 
 * Subscribe to conversation messages:
 * /topic/conversations/{conversationId}/messages
 * 
 * Send message:
 * /app/conversations/{conversationId}/messages
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming message from WebSocket client
     * Client sends to: /app/conversations/{conversationId}/messages
     */
    @MessageMapping("/conversations/{conversationId}/messages")
    public void handleMessage(@DestinationVariable String conversationId,
                             @Payload SendMessageRequest request) {
        log.info("Received WebSocket message for conversation: {}", conversationId);

        try {
            // Save message via service
            ChatMessageEntity message = messageService.sendMessage(
                    conversationId,
                    request.senderType(),
                    request.senderId(),
                    request.messageType(),
                    request.content(),
                    request.clientMessageId()
            );

            // Broadcast to all subscribers of this conversation
            MessageResponse response = MessageResponse.from(message);
            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + conversationId + "/messages",
                    response
            );

            log.info("Broadcasted message {} to conversation {}", message.getMessageId(), conversationId);

        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage(), e);
            // Send error to user
            messagingTemplate.convertAndSendToUser(
                    request.senderId(),
                    "/queue/errors",
                    "Failed to send message: " + e.getMessage()
            );
        }
    }

    /**
     * Broadcast message to conversation topic
     * Called by MessageService after saving message
     */
    public void broadcastMessage(String conversationId, MessageResponse message) {
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversationId + "/messages",
                message
        );
        log.info("Broadcasted message {} to conversation topic", message.messageId());
    }

    /**
     * Send typing indicator
     * Client sends to: /app/conversations/{conversationId}/typing
     */
    @MessageMapping("/conversations/{conversationId}/typing")
    public void handleTyping(@DestinationVariable String conversationId,
                            @Payload TypingIndicator indicator) {
        log.debug("User {} is typing in conversation {}", indicator.userId(), conversationId);
        
        // Broadcast typing indicator to other users
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversationId + "/typing",
                indicator
        );
    }

    /**
     * DTO for typing indicator
     */
    public record TypingIndicator(
            String userId,
            String userName,
            boolean isTyping
    ) {}
}
