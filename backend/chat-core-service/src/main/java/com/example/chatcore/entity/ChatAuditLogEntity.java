package com.example.chatcore.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_AUDIT_LOG")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatAuditLogEntity {

    @Id
    @Column(name = "ID", length = 36)
    private String id;

    @Column(name = "CONVERSATION_ID", nullable = false, length = 36)
    private String conversationId;

    @Column(name = "EVENT_TYPE", nullable = false, length = 64)
    private String eventType;

    @Column(name = "ACTOR_ID", length = 64)
    private String actorId;

    @Column(name = "ACTOR_TYPE", length = 16)
    private String actorType; // USER / AGENT / SYSTEM

    @Column(name = "OLD_VALUE", columnDefinition = "CLOB")
    private String oldValue;

    @Column(name = "NEW_VALUE", columnDefinition = "CLOB")
    private String newValue;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ChatAuditLogEntity create(String id, String conversationId, String eventType,
                                      String actorId, String actorType,
                                      String oldValue, String newValue) {
        ChatAuditLogEntity log = new ChatAuditLogEntity();
        log.id = id;
        log.conversationId = conversationId;
        log.eventType = eventType;
        log.actorId = actorId;
        log.actorType = actorType;
        log.oldValue = oldValue;
        log.newValue = newValue;
        log.createdAt = LocalDateTime.now();
        return log;
    }
}
