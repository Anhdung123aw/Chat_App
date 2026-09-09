package com.example.chatcore.dto;

import com.example.chatcore.entity.ChatCannedReplyEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CannedReplyDTO {

    /**
     * Request: Tạo canned reply
     */
    public record CreateCannedReplyRequest(
            @NotBlank(message = "Category không được trống") String category,
            @NotBlank(message = "Title không được trống") 
            @Size(max = 200) String title,
            @NotBlank(message = "Content không được trống") String content,
            String shortcuts,
            String locale,
            String createdBy
    ) {}

    /**
     * Request: Update canned reply
     */
    public record UpdateCannedReplyRequest(
            String category,
            @Size(max = 200) String title,
            String content,
            String shortcuts,
            String locale
    ) {}

    /**
     * Response: Canned reply
     */
    public record CannedReplyResponse(
            String replyId,
            String category,
            String title,
            String content,
            String shortcuts,
            String locale,
            String createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static CannedReplyResponse from(ChatCannedReplyEntity entity) {
            return new CannedReplyResponse(
                    entity.getReplyId(),
                    entity.getCategory(),
                    entity.getTitle(),
                    entity.getContent(),
                    entity.getShortcuts(),
                    entity.getLocale(),
                    entity.getCreatedBy(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
            );
        }
    }

    /**
     * Response: List canned replies
     */
    public record CannedReplyListResponse(
            List<CannedReplyResponse> replies,
            Integer total
    ) {}

    /**
     * Response: Canned reply categories
     */
    public record CannedReplyCategoriesResponse(
            List<String> categories
    ) {
        public static CannedReplyCategoriesResponse of(List<String> categories) {
            return new CannedReplyCategoriesResponse(categories);
        }
    }
}
