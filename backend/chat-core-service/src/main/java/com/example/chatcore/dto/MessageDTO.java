package com.example.chatcore.dto;

import com.example.chatcore.entity.ChatMessageEntity;
import com.example.chatcore.enums.MessageType;
import com.example.chatcore.enums.SenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class MessageDTO {

    public record SendMessageRequest(
            @NotBlank(message = "Sender ID is required") String senderId,
            @NotNull(message = "Sender type is required") SenderType senderType,
            @NotNull(message = "Message type is required") MessageType messageType,
            @Size(max = 4000, message = "Content must not exceed 4000 characters") String content,
            @NotBlank(message = "Client message ID is required") String clientMessageId
    ) {}

    public record MessageResponse(
            String messageId,
            String conversationId,
            SenderType senderType,
            String senderId,
            MessageType messageType,
            String content,
            String clientMessageId,
            Long seqNo,
            LocalDateTime createdAt
    ) {
        public static MessageResponse from(ChatMessageEntity m) {
            return new MessageResponse(
                    m.getMessageId(), m.getConversationId(), m.getSenderType(), m.getSenderId(),
                    m.getMessageType(), m.getContent(), m.getClientMessageId(), m.getSeqNo(), m.getCreatedAt());
        }
    }
}
