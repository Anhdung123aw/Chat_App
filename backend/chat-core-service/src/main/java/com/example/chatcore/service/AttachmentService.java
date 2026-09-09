package com.example.chatcore.service;

import com.example.chatcore.dto.AttachmentDTO.*;
import com.example.chatcore.entity.ChatAttachmentEntity;
import com.example.chatcore.repository.ChatAttachmentRepository;
import com.example.chatcore.exception.ResourceNotFoundException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final MinioClient minioClient;
    private final ChatAttachmentRepository attachmentRepository;

    @Value("${minio.bucket}")
    private String bucket;

    private static final int UPLOAD_URL_EXPIRY_SECONDS = 300; // 5 minutes
    private static final int DOWNLOAD_URL_EXPIRY_SECONDS = 3600; // 1 hour

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "text/plain"
    );

    /**
     * Section 15: Client request upload URL -> Attachment Service
     * Validate whitelist, file size, generate presigned URL
     */
    public UploadRequestResponse requestUpload(UploadRequestRequest request) {
        // Validate content type whitelist
        if (!ALLOWED_CONTENT_TYPES.contains(request.contentType())) {
            throw new IllegalArgumentException("Content type not allowed: " + request.contentType());
        }

        // Validate file size
        if (request.fileSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed: " + MAX_FILE_SIZE);
        }

        String attachmentId = UUID.randomUUID().toString();
        String objectKey = generateObjectKey(request.conversationId(), attachmentId, request.fileName());

        try {
            // Generate presigned PUT URL for client to upload directly to MinIO
            String uploadUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(UPLOAD_URL_EXPIRY_SECONDS, TimeUnit.SECONDS)
                            .build()
            );

            log.info("Generated upload URL for attachment: {} in conversation: {}", attachmentId, request.conversationId());

            return new UploadRequestResponse(attachmentId, uploadUrl, objectKey, UPLOAD_URL_EXPIRY_SECONDS);

        } catch (Exception e) {
            log.error("Failed to generate upload URL", e);
            throw new RuntimeException("Failed to generate upload URL", e);
        }
    }

    /**
     * Save attachment metadata after client confirms upload
     */
    @Transactional
    public ChatAttachmentEntity saveAttachment(String attachmentId, String messageId, String conversationId,
                                         String fileName, String fileType, Long fileSize, String objectKey) {
        ChatAttachmentEntity attachment = ChatAttachmentEntity.create(
                attachmentId, messageId, conversationId, fileName, fileType, fileSize, objectKey);
        return attachmentRepository.save(attachment);
    }

    /**
     * Get attachment with download URL
     */
    public AttachmentResponse getAttachment(String attachmentId) {
        ChatAttachmentEntity attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));

        String downloadUrl = generateDownloadUrl(attachment.getObjectKey());
        return AttachmentResponse.from(attachment, downloadUrl);
    }

    /**
     * List attachments by message
     */
    public List<AttachmentResponse> listByMessage(String messageId) {
        return attachmentRepository.findByMessageId(messageId).stream()
                .map(a -> AttachmentResponse.from(a, generateDownloadUrl(a.getObjectKey())))
                .toList();
    }

    /**
     * Generate signed download URL (Section 15: download dùng signed URL thời hạn ngắn)
     */
    private String generateDownloadUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(DOWNLOAD_URL_EXPIRY_SECONDS, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate download URL for object: {}", objectKey, e);
            return null;
        }
    }

    /**
     * Generate object key: conversations/{conversationId}/attachments/{attachmentId}/{fileName}
     */
    private String generateObjectKey(String conversationId, String attachmentId, String fileName) {
        return String.format("conversations/%s/attachments/%s/%s", conversationId, attachmentId, fileName);
    }
}
