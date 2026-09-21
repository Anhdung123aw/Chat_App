package com.example.realtime.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when a new message is created
 * Consumed from Kafka topic: chat.message.created
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreatedEvent {
    private String messageId;
    private String conversationId;
    private String senderId;
    private String senderType; // USER, AGENT, SYSTEM
    private String messageType; // TEXT, IMAGE, FILE, SYSTEM
    private String content;
    private Long sequence;
    private LocalDateTime createdAt;
    
    // Recipient information
    private String recipientId;
    private String recipientType; // USER, AGENT
}
