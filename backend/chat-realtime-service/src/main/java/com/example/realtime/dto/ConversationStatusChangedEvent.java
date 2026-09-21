package com.example.realtime.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when a conversation status changes
 * Consumed from Kafka topic: chat.conversation.status-changed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationStatusChangedEvent {
    private String conversationId;
    private String customerId;
    private String agentId;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime changedAt;
    private String reason; // RESOLVED, TIMEOUT, ESCALATED, etc.
}
