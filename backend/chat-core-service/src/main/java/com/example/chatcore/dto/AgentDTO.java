package com.example.chatcore.dto;

import com.example.chatcore.entity.ChatAgentEntity;
import com.example.chatcore.enums.AgentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentDTO {

    /**
     * Request: Cập nhật trạng thái agent
     */
    public record UpdateAgentStatusRequest(
            @NotNull(message = "Status không được null") AgentStatus status
    ) {}

    /**
     * Request: Cập nhật capacity agent
     */
    public record UpdateAgentCapacityRequest(
            @Min(value = 1, message = "Max concurrent chats phải >= 1")
            @Max(value = 20, message = "Max concurrent chats phải <= 20")
            Integer maxConcurrentChats
    ) {}

    /**
     * Request: Tạo agent mới
     */
    public record CreateAgentRequest(
            @NotBlank(message = "Agent name không được trống") String name,
            @Email(message = "Email không hợp lệ") String email,
            String skills,
            @Min(1) @Max(20) Integer maxConcurrentChats,
            String createdBy
    ) {}

    /**
     * Request: Heartbeat từ agent
     */
    public record AgentHeartbeatRequest(
            @NotBlank(message = "Agent ID không được trống") String agentId
    ) {}

    /**
     * Response: Thông tin agent
     */
    public record AgentResponse(
            String agentId,
            String agentName,
            String email,
            AgentStatus status,
            String skills,
            Integer maxConcurrentChats,
            Integer currentChatCount,
            Boolean canAcceptMoreChats,
            LocalDateTime lastActiveAt,
            LocalDateTime createdAt
    ) {
        public static AgentResponse from(ChatAgentEntity entity) {
            return new AgentResponse(
                    entity.getAgentId(),
                    entity.getAgentName(),
                    entity.getEmail(),
                    entity.getStatus(),
                    entity.getSkills(),
                    entity.getMaxConcurrentChats(),
                    entity.getCurrentChatCount(),
                    entity.canAcceptMoreChats(),
                    entity.getLastActiveAt(),
                    entity.getCreatedAt()
            );
        }
    }

    // Alias for backward compatibility
    public record AgentInfoResponse(
            String agentId,
            String agentName,
            String email,
            AgentStatus status,
            String skills,
            Integer maxConcurrentChats,
            Integer currentChatCount,
            Boolean canAcceptMoreChats,
            LocalDateTime lastActiveAt,
            LocalDateTime createdAt
    ) {
        public static AgentInfoResponse from(ChatAgentEntity entity) {
            return new AgentInfoResponse(
                    entity.getAgentId(),
                    entity.getAgentName(),
                    entity.getEmail(),
                    entity.getStatus(),
                    entity.getSkills(),
                    entity.getMaxConcurrentChats(),
                    entity.getCurrentChatCount(),
                    entity.canAcceptMoreChats(),
                    entity.getLastActiveAt(),
                    entity.getCreatedAt()
            );
        }
    }

    /**
     * Response: Danh sách agents có sẵn
     */
    public record AvailableAgentsResponse(
            List<AgentResponse> agents,
            Integer totalAvailable
    ) {}

    /**
     * Response: Thống kê agent
     */
    public record AgentStatsResponse(
            String agentId,
            String agentName,
            Integer totalChatsHandled,
            Integer currentChats,
            Double avgHandlingTime,
            Double csatScore
    ) {}
}
