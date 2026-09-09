package com.example.chatcore.dto;

import com.example.chatcore.entity.ChatAttachmentEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttachmentDTO {

    /**
     * Request để tạo upload URL (client sẽ upload trực tiếp lên MinIO)
     * Section 15: Attachment - Client request upload URL -> upload trực tiếp -> MinIO
     */
    public record UploadRequestRequest(
            @NotBlank String conversationId,
            @NotBlank String fileName,
            @NotBlank String contentType,
            @NotNull @Positive Long fileSize
    ) {
    }

    /**
     * Response chứa presigned URL để client upload
     */
    public record UploadRequestResponse(
            String attachmentId,
            String uploadUrl,
            String objectKey,
            int expiresInSeconds
    ) {
    }

    /**
     * Response cho attachment metadata
     */
    public record AttachmentResponse(
            String attachmentId,
            String messageId,
            String conversationId,
            String fileName,
            String fileType,
            Long fileSize,
            String objectKey,
            String downloadUrl,
            LocalDateTime createdAt
    ) {
        public static AttachmentResponse from(ChatAttachmentEntity attachment, String downloadUrl) {
            return new AttachmentResponse(
                    attachment.getAttachmentId(),
                    attachment.getMessageId(),
                    attachment.getConversationId(),
                    attachment.getFileName(),
                    attachment.getFileType(),
                    attachment.getFileSize(),
                    attachment.getObjectKey(),
                    downloadUrl,
                    attachment.getCreatedAt()
            );
        }
    }

    /**
     * Whitelist MIME types theo section 15
     */
    public static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain", "text/csv"
    );

    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
}
