package com.example.chatcore.dto;

import com.example.chatcore.entity.ChatContextEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContextDTO {

    /**
     * Context capture từ client khi tạo conversation.
     * Theo tài liệu section 6: whitelist context, không truyền nguyên business object.
     */
    public record ChatContextRequest(
            @Size(max = 128, message = "Screen name must not exceed 128 characters") String screenName,
            @Size(max = 128, message = "Feature must not exceed 128 characters") String feature,
            @Size(max = 128, message = "Last action must not exceed 128 characters") String lastAction,
            @Size(max = 32, message = "App version must not exceed 32 characters") String appVersion,
            @Size(max = 32, message = "OS must not exceed 32 characters") String os,
            @Size(max = 128, message = "Device must not exceed 128 characters") String device,
            @Size(max = 4000, message = "Context JSON must not exceed 4000 characters") String contextJson
    ) {
    }

    public record ChatContextResponse(
            String conversationId,
            String screenName,
            String feature,
            String lastAction,
            String appVersion,
            String os,
            String device
    ) {
        public static ChatContextResponse from(ChatContextEntity context) {
            return new ChatContextResponse(
                    context.getConversationId(),
                    context.getScreenName(),
                    context.getFeature(),
                    context.getLastAction(),
                    context.getAppVersion(),
                    context.getOs(),
                    context.getDevice()
            );
        }
    }
}
