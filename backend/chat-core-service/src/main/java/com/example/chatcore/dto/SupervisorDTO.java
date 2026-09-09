package com.example.chatcore.dto;

import com.example.chatcore.enums.AgentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupervisorDTO {

    /**
     * Response: Tổng quan hệ thống (Supervisor Dashboard - detailed version)
     */
    public record SupervisorOverviewResponse(
            AgentMetricsDetailed agentMetrics,
            QueueMetricsDetailed queueMetrics,
            SlaMetricsDetailed slaMetrics,
            PerformanceMetricsDetailed performanceMetrics,
            LocalDateTime timestamp
    ) {}

    /**
     * Metrics về agents (detailed)
     */
    public record AgentMetricsDetailed(
            Integer totalAgents,
            Integer onlineAgents,
            Integer busyAgents,
            Integer availableAgents,
            Double avgChatsPerAgent,
            Double totalCapacity,
            Double usedCapacity
    ) {}

    /**
     * Metrics về queue (detailed)
     */
    public record QueueMetricsDetailed(
            Integer totalInQueue,
            Integer newConversations,
            Integer inProgressConversations,
            Integer waitingCustomerConversations,
            Map<String, Integer> queueByTopic,
            Double avgWaitTimeSeconds
    ) {}

    /**
     * Metrics về SLA (detailed)
     */
    public record SlaMetricsDetailed(
            Long totalConversationsToday,
            Long slaBreachesToday,
            Long atRiskConversations,
            Double slaComplianceRate
    ) {}

    /**
     * Metrics về performance (detailed)
     */
    public record PerformanceMetricsDetailed(
            Double avgFirstResponseTime,
            Double avgHandlingTime,
            Double csatScore,
            Long totalClosedToday
    ) {}

    /**
     * Response: Danh sách agents với status
     */
    public record AgentStatusListResponse(
            List<AgentStatusSummary> agents,
            Integer total
    ) {}

    /**
     * Summary của 1 agent
     */
    public record AgentStatusSummary(
            String agentId,
            String agentName,
            AgentStatus status,
            Integer currentChats,
            Integer maxChats,
            Double utilizationRate,
            LocalDateTime lastActiveAt
    ) {}

    /**
     * Response: Thống kê queue
     */
    public record QueueStatsResponse(
            Integer totalInQueue,
            Map<String, Integer> byTopic,
            Map<String, Integer> byStatus,
            Double avgWaitTime,
            Integer oldestWaitTimeSeconds
    ) {}

    /**
     * Request: Reassign conversation
     */
    public record ReassignConversationRequest(
            @NotBlank(message = "Conversation ID không được trống") String conversationId,
            @NotBlank(message = "To Agent ID không được trống") String toAgentId,
            @NotBlank(message = "Reassigned by không được trống") String reassignedBy,
            String reason
    ) {}

    /**
     * Response: Agent performance detail
     */
    public record AgentPerformanceResponse(
            String agentId,
            String agentName,
            Long totalChatsHandled,
            Double avgFirstResponseTime,
            Double avgHandlingTime,
            Double csatScore,
            Long totalRatings,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {}

    /**
     * Simplified Agent Metrics
     */
    public record AgentMetrics(
            Long totalOnline,
            Long totalAvailable,
            Long totalBusy
    ) {}

    /**
     * Simplified Queue Metrics
     */
    public record QueueMetrics(
            Long queueDepth,
            Long avgWaitTimeSeconds,
            Long oldestWaitTimeSeconds
    ) {}

    /**
     * Simplified SLA Metrics
     */
    public record SlaMetrics(
            Long violationsCount,
            Long atRiskCount,
            Double complianceRate
    ) {}

    /**
     * Simplified Performance Metrics
     */
    public record PerformanceMetrics(
            Double avgFirstResponseTime,
            Double avgHandlingTime,
            Double csatPercentage
    ) {}

    /**
     * Response: Dashboard Overview (simplified for controller)
     */
    public record DashboardOverviewResponse(
            AgentMetrics agentMetrics,
            QueueMetrics queueMetrics,
            SlaMetrics slaMetrics,
            PerformanceMetrics performanceMetrics,
            LocalDateTime timestamp
    ) {}

    /**
     * Response: Queue Detail
     */
    public record QueueDetailResponse(
            Long queueDepth,
            List<ConversationSummary> conversations
    ) {}

    /**
     * Conversation Summary
     */
    public record ConversationSummary(
            String conversationId,
            String topicCode,
            LocalDateTime createdAt,
            LocalDateTime slaDeadline
    ) {}

    /**
     * Response: Performance Report
     */
    public record PerformanceReportResponse(
            Double avgFirstResponseTime,
            Double avgHandlingTime,
            Double csatPercentage,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {}

    /**
     * Response: Batch Assign
     */
    public record BatchAssignResponse(
            Integer assignedCount,
            String message
    ) {}
}
