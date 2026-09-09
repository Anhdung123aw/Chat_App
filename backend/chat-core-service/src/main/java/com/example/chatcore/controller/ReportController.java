package com.example.chatcore.controller;

import com.example.chatcore.dto.ReportDTO.*;
import com.example.chatcore.service.ReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST Controller cho Reporting
 * APIs báo cáo vận hành: FRT, AHT, CSAT, SLA, Top Topics, Backlog
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportingService reportingService;

    /**
     * GET /api/v1/reports/frt - First Response Time Report
     */
    @GetMapping("/frt")
    public ResponseEntity<FrtReportResponse> getFrtReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting FRT report from {} to {}", startDate, endDate);
        FrtReportResponse report = reportingService.calculateFrt(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/v1/reports/aht - Average Handling Time Report
     */
    @GetMapping("/aht")
    public ResponseEntity<AhtReportResponse> getAhtReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting AHT report from {} to {}", startDate, endDate);
        AhtReportResponse report = reportingService.calculateAht(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/v1/reports/csat - CSAT Report
     */
    @GetMapping("/csat")
    public ResponseEntity<CsatReportResponse> getCsatReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting CSAT report from {} to {}", startDate, endDate);
        CsatReportResponse report = reportingService.calculateCsat(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/v1/reports/sla-breach - SLA Breach Report
     */
    @GetMapping("/sla-breach")
    public ResponseEntity<SlaBreachReportResponse> getSlaBreachReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting SLA breach report from {} to {}", startDate, endDate);
        SlaBreachReportResponse report = reportingService.calculateSlaBreach(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/v1/reports/top-topics - Top Topics Report
     */
    @GetMapping("/top-topics")
    public ResponseEntity<TopTopicsReportResponse> getTopTopicsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Getting top {} topics from {} to {}", limit, startDate, endDate);
        TopTopicsReportResponse report = reportingService.getTopTopics(startDate, endDate, limit);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/v1/reports/agent-performance/{agentId} - Agent Performance Report
     */
    @GetMapping("/agent-performance/{agentId}")
    public ResponseEntity<AgentPerformanceReportResponse> getAgentPerformanceReport(
            @PathVariable String agentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting agent {} performance from {} to {}", agentId, startDate, endDate);
        AgentPerformanceReportResponse report = reportingService.getAgentPerformance(agentId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/v1/reports/backlog - Backlog Report
     */
    @GetMapping("/backlog")
    public ResponseEntity<BacklogReportResponse> getBacklogReport() {
        log.info("Getting backlog report");
        BacklogReportResponse report = reportingService.getBacklogReport();
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/v1/reports/dashboard - Comprehensive Dashboard Report
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardReportResponse> getDashboardReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting comprehensive dashboard report from {} to {}", startDate, endDate);
        DashboardReportResponse report = reportingService.getDashboardReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
