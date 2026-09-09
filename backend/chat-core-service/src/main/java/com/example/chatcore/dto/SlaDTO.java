package com.example.chatcore.dto;

import com.example.chatcore.entity.ChatSlaConfigEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlaDTO {

    /**
     * Request: Tạo SLA config
     */
    public record CreateSlaConfigRequest(
            @NotBlank(message = "Topic code không được trống") String topic,
            @NotNull @Min(1) Integer firstResponseTimeSeconds,
            Integer resolutionTimeSeconds
    ) {}

    /**
     * Request: Update SLA config
     */
    public record UpdateSlaConfigRequest(
            Integer firstResponseTimeSeconds,
            Integer resolutionTimeSeconds
    ) {}

    /**
     * Request: Tạo/Update SLA config (legacy)
     */
    public record SlaConfigRequest(
            @NotBlank(message = "Topic code không được trống") String topicCode,
            @NotNull @Min(1) Integer firstResponseTimeSeconds,
            Integer resolutionTimeSeconds,
            Boolean enabled
    ) {}

    /**
     * Response: SLA config
     */
    public record SlaConfigResponse(
            String configId,
            String topicCode,
            Integer firstResponseTimeSeconds,
            Integer resolutionTimeSeconds,
            Boolean enabled,
            LocalDateTime createdAt
    ) {
        public static SlaConfigResponse from(ChatSlaConfigEntity entity) {
            return new SlaConfigResponse(
                    entity.getConfigId(),
                    entity.getTopicCode(),
                    entity.getFirstResponseTimeSeconds(),
                    entity.getResolutionTimeSeconds(),
                    entity.getEnabled(),
                    entity.getCreatedAt()
            );
        }
    }

    /**
     * Response: Conversation vi phạm SLA
     */
    public record SlaViolationResponse(
            String conversationId,
            String topicCode,
            LocalDateTime slaDeadline,
            LocalDateTime violatedAt,
            Long violationSeconds,
            String assignedAgent
    ) {}

    /**
     * Response: Conversations sắp vi phạm SLA
     */
    public record SlaAtRiskResponse(
            String conversationId,
            String topicCode,
            LocalDateTime slaDeadline,
            Long remainingSeconds,
            String assignedAgent
    ) {}

    /**
     * Response: Thống kê SLA
     */
    public record SlaStatisticsResponse(
            Long totalConversations,
            Long violatedConversations,
            Long atRiskConversations,
            Double complianceRate,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {}

    /**
     * Response: List violations
     */
    public record SlaViolationsListResponse(
            List<SlaViolationResponse> violations,
            Integer totalViolations
    ) {}
}
