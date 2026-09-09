package com.example.chatcore.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_ATTACHMENT")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatAttachmentEntity {

    @Id
    @Column(name = "ATTACHMENT_ID", length = 36)
    private String attachmentId;

    @Column(name = "MESSAGE_ID", nullable = false, length = 36)
    private String messageId;

    @Column(name = "CONVERSATION_ID", nullable = false, length = 36)
    private String conversationId;

    @Column(name = "FILE_NAME", length = 255)
    private String fileName;

    @Column(name = "FILE_TYPE", length = 128)
    private String fileType;

    @Column(name = "FILE_SIZE")
    private Long fileSize;

    @Column(name = "OBJECT_KEY", length = 512)
    private String objectKey;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ChatAttachmentEntity create(String attachmentId, String messageId, String conversationId,
                                        String fileName, String fileType, Long fileSize, String objectKey) {
        ChatAttachmentEntity a = new ChatAttachmentEntity();
        a.attachmentId = attachmentId;
        a.messageId = messageId;
        a.conversationId = conversationId;
        a.fileName = fileName;
        a.fileType = fileType;
        a.fileSize = fileSize;
        a.objectKey = objectKey;
        a.createdAt = LocalDateTime.now();
        return a;
    }
}
