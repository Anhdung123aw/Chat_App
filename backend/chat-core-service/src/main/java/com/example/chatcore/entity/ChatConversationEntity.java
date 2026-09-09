package com.example.chatcore.entity;

import com.example.chatcore.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_CONVERSATION")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatConversationEntity {

    @Id
    @Column(name = "CONVERSATION_ID", length = 36)
    private String conversationId;

    @Column(name = "USER_ID", nullable = false, length = 64)
    private String userId;

    @Column(name = "MERCHANT_ID", length = 64)
    private String merchantId;

    @Column(name = "TOPIC_CODE", length = 64)
    private String topicCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 32)
    private ConversationStatus status;

    @Column(name = "ASSIGNED_AGENT", length = 64)
    private String assignedAgent;

    @Column(name = "SLA_DEADLINE")
    private LocalDateTime slaDeadline;

    @Column(name = "FIRST_RESPONSE_AT")
    private LocalDateTime firstResponseAt;

    @Column(name = "CLOSED_AT")
    private LocalDateTime closedAt;

    @Column(name = "LAST_MESSAGE_ID", length = 36)
    private String lastMessageId;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    public static ChatConversationEntity createNew(String conversationId, String userId, String merchantId, String topicCode) {
        ChatConversationEntity c = new ChatConversationEntity();
        c.conversationId = conversationId;
        c.userId = userId;
        c.merchantId = merchantId;
        c.topicCode = topicCode;
        c.status = ConversationStatus.NEW;
        LocalDateTime now = LocalDateTime.now();
        c.createdAt = now;
        c.updatedAt = now;
        return c;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}