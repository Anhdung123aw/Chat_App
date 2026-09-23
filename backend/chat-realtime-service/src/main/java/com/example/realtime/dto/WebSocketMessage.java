package com.example.realtime.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket Message envelope
 * All messages sent/received via WebSocket follow this structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage {
    private MessageType type;
    private String eventId;
    private String conversationId;
    private Long sequence;
    private LocalDateTime timestamp;
    private Map<String, Object> payload;
    private String senderId;
    private String senderType; // USER, AGENT, SYSTEM
    private String errorCode;
    private String errorMessage;

    public enum MessageType {
        // Connection
        CONNECT,
        DISCONNECT,
        PING,
        PONG,
        
        // Subscription
        SUBSCRIBE,
        UNSUBSCRIBE,
        SUBSCRIBED,
        UNSUBSCRIBED,
        
        // Messages
        MESSAGE,
        MESSAGE_ACK,
        MESSAGE_DELIVERED,
        MESSAGE_READ,
        
        // Typing
        TYPING_START,
        TYPING_STOP,
        
        // Presence
        PRESENCE_UPDATE,
        USER_ONLINE,
        USER_OFFLINE,
        AGENT_ONLINE,
        AGENT_OFFLINE,
        
        // Conversation
        CONVERSATION_ASSIGNED,
        CONVERSATION_STATUS_CHANGED,
        CONVERSATION_CLOSED,
        
        // Error
        ERROR
    }
}
