package com.example.chatcore.controller;

import com.example.chatcore.dto.SlaDTO.*;
import com.example.chatcore.service.SlaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller cho SLA Management
 * APIs quản lý SLA config, violations, at-risk conversations
 */
@RestController
@RequestMapping("/api/v1/sla")
@RequiredArgsConstructor
@Slf4j
public class SlaController {

    private final SlaService slaService;

    /**
     * POST /api/v1/sla/configs - Tạo SLA config mới
     */
    @PostMapping("/configs")
    public ResponseEntity<SlaConfigResponse> createSlaConfig(@Valid @RequestBody CreateSlaConfigRequest request) {
        log.info("Creating SLA config for topic: {}", request.topic());
        SlaConfigResponse response = slaService.createSlaConfig(request);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/sla/configs/{configId} - Update SLA config
     */
    @PutMapping("/configs/{configId}")
    public ResponseEntity<SlaConfigResponse> updateSlaConfig(
            @PathVariable String configId,
            @Valid @RequestBody UpdateSlaConfigRequest request) {
        log.info("Updating SLA config: {}", configId);
        SlaConfigResponse response = slaService.updateSlaConfig(configId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/sla/configs/{topicCode} - Get SLA config by topic
     */
    @GetMapping("/configs/{topicCode}")
    public ResponseEntity<SlaConfigResponse> getSlaConfigByTopic(@PathVariable String topicCode) {
        SlaConfigResponse response = slaService.getSlaConfigByTopic(topicCode);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/sla/configs - List all SLA configs
     */
    @GetMapping("/configs")
    public ResponseEntity<List<SlaConfigResponse>> listAllSlaConfigs() {
        List<SlaConfigResponse> configs = slaService.listAllSlaConfigs();
        return ResponseEntity.ok(configs);
    }

    /**
     * GET /api/v1/sla/configs/default - Get default SLA config
     */
    @GetMapping("/configs/default")
    public ResponseEntity<SlaConfigResponse> getDefaultSlaConfig() {
        SlaConfigResponse response = slaService.getDefaultSlaConfig();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/sla/violations - Get SLA violations
     */
    @GetMapping("/violations")
    public ResponseEntity<List<SlaViolationResponse>> getSlaViolations() {
        List<SlaViolationResponse> violations = slaService.getSlaViolations();
        return ResponseEntity.ok(violations);
    }

    /**
     * GET /api/v1/sla/at-risk - Get conversations at risk
     */
    @GetMapping("/at-risk")
    public ResponseEntity<List<SlaAtRiskResponse>> getAtRiskConversations(
            @RequestParam(defaultValue = "5") int minutesThreshold) {
        List<SlaAtRiskResponse> atRisk = slaService.getAtRiskConversations(minutesThreshold);
        return ResponseEntity.ok(atRisk);
    }

    /**
     * GET /api/v1/sla/statistics - Get SLA statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<SlaStatisticsResponse> getSlaStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        SlaStatisticsResponse statistics = slaService.getSlaStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * GET /api/v1/sla/conversations/{conversationId}/check - Check conversation SLA violation
     */
    @GetMapping("/conversations/{conversationId}/check")
    public ResponseEntity<Boolean> checkConversationSlaViolation(@PathVariable String conversationId) {
        boolean violated = slaService.checkSlaViolation(conversationId);
        return ResponseEntity.ok(violated);
    }
}
