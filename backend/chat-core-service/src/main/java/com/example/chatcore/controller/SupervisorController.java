package com.example.chatcore.controller;

import com.example.chatcore.dto.SupervisorDTO.*;
import com.example.chatcore.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller cho Supervisor Dashboard
 * APIs tổng hợp cho Supervisor giám sát vận hành
 */
@RestController
@RequestMapping("/api/v1/supervisor")
@RequiredArgsConstructor
@Slf4j
public class SupervisorController {

    private final AgentPresenceService agentPresenceService;
    private final AutoAssignmentService autoAssignmentService;
    private final SlaService slaService;
    private final ReportingService reportingService;
    private final RatingService ratingService;
    private final AlertService alertService;

    /**
     * GET /api/v1/supervisor/dashboard - Get dashboard overview
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardOverviewResponse> getDashboardOverview() {
        log.info("Getting supervisor dashboard overview");

        // Agent metrics
        List<com.example.chatcore.dto.AgentDTO.AgentResponse> onlineAgents = agentPresenceService.getOnlineAgents();
        List<com.example.chatcore.dto.AgentDTO.AgentResponse> availableAgents = agentPresenceService.getAvailableAgents();

        AgentMetrics agentMetrics = new AgentMetrics(
                (long) onlineAgents.size(),
                (long) availableAgents.size(),
                (long) onlineAgents.size() - availableAgents.size()
        );

        // Queue metrics
        long queueDepth = autoAssignmentService.getQueueDepth();
        QueueMetrics queueMetrics = new QueueMetrics(queueDepth, 0L, 0L); // TODO: Calculate avg wait time

        // SLA metrics
        List<com.example.chatcore.dto.SlaDTO.SlaViolationResponse> violations = slaService.getSlaViolations();
        List<com.example.chatcore.dto.SlaDTO.SlaAtRiskResponse> atRisk = slaService.getAtRiskConversations(5);
        SlaMetrics slaMetrics = new SlaMetrics(
                (long) violations.size(),
                (long) atRisk.size(),
                0.0 // TODO: Calculate compliance rate
        );

        // CSAT metrics
        com.example.chatcore.dto.RatingDTO.CsatScoreResponse csatScore = ratingService.calculateCsatScore();
        PerformanceMetrics performanceMetrics = new PerformanceMetrics(
                0.0, // TODO: Calculate avg FRT
                0.0, // TODO: Calculate avg AHT
                csatScore.csatPercentage()
        );

        DashboardOverviewResponse response = new DashboardOverviewResponse(
                agentMetrics,
                queueMetrics,
                slaMetrics,
                performanceMetrics,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/supervisor/agents/online - Get online agents detail
     */
    @GetMapping("/agents/online")
    public ResponseEntity<List<com.example.chatcore.dto.AgentDTO.AgentResponse>> getOnlineAgentsDetail() {
        List<com.example.chatcore.dto.AgentDTO.AgentResponse> agents = agentPresenceService.getOnlineAgents();
        return ResponseEntity.ok(agents);
    }

    /**
     * GET /api/v1/supervisor/queue - Get queue detail
     */
    @GetMapping("/queue")
    public ResponseEntity<QueueDetailResponse> getQueueDetail() {
        long queueDepth = autoAssignmentService.getQueueDepth();
        List<com.example.chatcore.entity.ChatConversationEntity> unassignedConversations = 
                autoAssignmentService.getUnassignedConversations();

        QueueDetailResponse response = new QueueDetailResponse(
                queueDepth,
                unassignedConversations.stream()
                        .map(conv -> new ConversationSummary(
                                conv.getConversationId(),
                                conv.getTopicCode(),
                                conv.getCreatedAt(),
                                conv.getSlaDeadline()
                        ))
                        .toList()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/supervisor/sla/violations - Get SLA violations detail
     */
    @GetMapping("/sla/violations")
    public ResponseEntity<List<com.example.chatcore.dto.SlaDTO.SlaViolationResponse>> getSlaViolationsDetail() {
        List<com.example.chatcore.dto.SlaDTO.SlaViolationResponse> violations = slaService.getSlaViolations();
        return ResponseEntity.ok(violations);
    }

    /**
     * GET /api/v1/supervisor/sla/at-risk - Get at-risk conversations detail
     */
    @GetMapping("/sla/at-risk")
    public ResponseEntity<List<com.example.chatcore.dto.SlaDTO.SlaAtRiskResponse>> getAtRiskConversationsDetail(
            @RequestParam(defaultValue = "5") int minutesThreshold) {
        List<com.example.chatcore.dto.SlaDTO.SlaAtRiskResponse> atRisk = 
                slaService.getAtRiskConversations(minutesThreshold);
        return ResponseEntity.ok(atRisk);
    }

    /**
     * GET /api/v1/supervisor/performance - Get performance metrics
     */
    @GetMapping("/performance")
    public ResponseEntity<PerformanceReportResponse> getPerformanceMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        com.example.chatcore.dto.ReportDTO.FrtReportResponse frt = reportingService.calculateFrt(startDate, endDate);
        com.example.chatcore.dto.ReportDTO.AhtReportResponse aht = reportingService.calculateAht(startDate, endDate);
        com.example.chatcore.dto.RatingDTO.CsatReportResponse csat = ratingService.calculateCsatReport(startDate, endDate);

        PerformanceReportResponse response = new PerformanceReportResponse(
                frt.avgFirstResponseTimeSeconds(),
                aht.avgHandlingTimeSeconds(),
                csat.csatPercentage(),
                startDate,
                endDate
        );

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/supervisor/batch-assign - Batch auto assign conversations
     */
    @PostMapping("/batch-assign")
    public ResponseEntity<BatchAssignResponse> batchAutoAssign() {
        log.info("Starting batch auto assignment");
        int assignedCount = autoAssignmentService.batchAutoAssign();
        return ResponseEntity.ok(new BatchAssignResponse(assignedCount, "Batch assignment completed"));
    }

    /**
     * GET /api/v1/supervisor/alerts/check - Check all alerts
     */
    @GetMapping("/alerts/check")
    public ResponseEntity<List<com.example.chatcore.service.AlertService.AlertMessage>> checkAllAlerts() {
        log.info("Checking all alerts for supervisor");
        List<com.example.chatcore.service.AlertService.AlertMessage> alerts = alertService.checkAllAlerts();
        return ResponseEntity.ok(alerts);
    }
}
