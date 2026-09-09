package com.example.chatcore.entity;

import com.example.chatcore.enums.MessageType;
import com.example.chatcore.enums.SenderType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_MESSAGE")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageEntity {

    @Id
    @Column(name = "MESSAGE_ID", length = 36)
    private String messageId;

    @Column(name = "CONVERSATION_ID", nullable = false, length = 36)
    private String conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "SENDER_TYPE", nullable = false, length = 16)
    private SenderType senderType;

    @Column(name = "SENDER_ID", nullable = false, length = 64)
    private String senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "MESSAGE_TYPE", nullable = false, length = 16)
    private MessageType messageType;

    @Column(name = "CONTENT", columnDefinition = "CLOB")
    private String content;

    @Column(name = "CLIENT_MESSAGE_ID", nullable = false, length = 64)
    private String clientMessageId;

    @Column(name = "SEQ_NO", nullable = false)
    private Long seqNo;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "DELIVERED_AT")
    private LocalDateTime deliveredAt;

    @Column(name = "READ_AT")
    private LocalDateTime readAt;

    public static ChatMessageEntity create(String messageId, String conversationId, SenderType senderType,
                                     String senderId, MessageType messageType, String content,
                                     String clientMessageId, long seqNo) {
        ChatMessageEntity m = new ChatMessageEntity();
        m.messageId = messageId;
        m.conversationId = conversationId;
        m.senderType = senderType;
        m.senderId = senderId;
        m.messageType = messageType;
        m.content = content;
        m.clientMessageId = clientMessageId;
        m.seqNo = seqNo;
        m.createdAt = LocalDateTime.now();
        return m;
    }
}