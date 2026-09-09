package com.example.chatcore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportDTO {

    /**
     * Response: First Response Time Report
     */
    public record FrtReportResponse(
            Double avgFirstResponseTimeSeconds,
            Long minFirstResponseTimeSeconds,
            Long maxFirstResponseTimeSeconds,
            Long totalConversations,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {}

    /**
     * Response: Average Handling Time Report
     */
    public record AhtReportResponse(
            Double avgHandlingTimeSeconds,
            Long minHandlingTimeSeconds,
            Long maxHandlingTimeSeconds,
            Long totalClosedConversations,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {}

    /**
     * Response: CSAT Report (simplified)
     */
    public record CsatReportResponse(
            Double averageRating,
            Double csatPercentage,
            Long totalRatings,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {}

    /**
     * Response: SLA Breach Report
     */
    public record SlaBreachReportResponse(
            Long totalConversations,
            Long breachedConversations,
            Double breachRate,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {}

    /**
     * Response: Top Topics Report
     */
    public record TopTopicsReportResponse(
            List<TopicCount> topTopics,
            Long totalConversations,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {}

    /**
     * Topic count
     */
    public record TopicCount(
            String topic,
            Long count
    ) {}

    /**
     * Response: Backlog Report
     */
    public record BacklogReportResponse(
            Long waitingCount,
            Long activeCount,
            Long totalBacklog,
            List<BacklogConversation> oldestConversations
    ) {}

    /**
     * Backlog conversation detail
     */
    public record BacklogConversation(
            String conversationId,
            String topic,
            LocalDateTime createdAt,
            Long waitingSeconds
    ) {}

    /**
     * Response: Agent Performance Report
     */
    public record AgentPerformanceReportResponse(
            String agentId,
            Long totalConversations,
            Long closedConversations,
            Double avgFirstResponseTime,
            Double avgHandlingTime,
            Double avgCsatScore,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {}

    /**
     * Response: Comprehensive Dashboard Report
     */
    public record DashboardReportResponse(
            FrtReportResponse frt,
            AhtReportResponse aht,
            CsatReportResponse csat,
            SlaBreachReportResponse slaBreach,
            TopTopicsReportResponse topTopics,
            BacklogReportResponse backlog,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {}

    /**
     * Phân bố rating
     */
    public record RatingDistribution(
            Long oneStar,
            Long twoStar,
            Long threeStar,
            Long fourStar,
            Long fiveStar
    ) {}

    /**
     * Response: Daily Stats
     */
    public record DailyStatsResponse(
            LocalDateTime date,
            Long totalConversations,
            Long closedConversations,
            Double avgFirstResponseTime,
            Double avgHandlingTime,
            Double csatScore,
            Long slaBreaches
    ) {}
}
