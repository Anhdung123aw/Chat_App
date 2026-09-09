package com.example.chatcore.scheduler;

import com.example.chatcore.dto.SlaDTO.*;
import com.example.chatcore.service.SlaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled Job - SLA Monitoring
 * Chạy mỗi 1 phút để check SLA violations và conversations at risk
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "scheduler.sla-monitoring.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SlaMonitoringJob {

    private final SlaService slaService;

    /**
     * Check SLA violations mỗi 1 phút
     * Cron: every minute (0 * * * * ?)
     */
    @Scheduled(cron = "${scheduler.sla-monitoring.cron:0 * * * * ?}")
    public void monitorSlaViolations() {
        try {
            log.debug("Running SLA monitoring job...");

            // Get current violations
            List<SlaViolationResponse> violations = slaService.getSlaViolations();
            
            if (!violations.isEmpty()) {
                log.warn("⚠️ Found {} SLA violations", violations.size());
                violations.forEach(v -> 
                    log.warn("  - Conversation {} violated SLA by {} seconds (topic: {})",
                            v.conversationId(), v.violationSeconds(), v.topicCode())
                );
            } else {
                log.debug("✅ No SLA violations found");
            }

            // Get at-risk conversations (sắp vi phạm trong 5 phút)
            List<SlaAtRiskResponse> atRisk = slaService.getAtRiskConversations(5);
            
            if (!atRisk.isEmpty()) {
                log.info("⏰ Found {} conversations at risk of SLA breach", atRisk.size());
                atRisk.forEach(r ->
                    log.info("  - Conversation {} has {} seconds remaining (topic: {})",
                            r.conversationId(), r.remainingSeconds(), r.topicCode())
                );
            }

            log.debug("SLA monitoring job completed. Violations: {}, At-risk: {}", 
                    violations.size(), atRisk.size());

        } catch (Exception e) {
            log.error("❌ Error in SLA monitoring job: {}", e.getMessage(), e);
        }
    }

    /**
     * Optional: Generate SLA report summary mỗi giờ
     * Cron: at minute 0 of every hour (0 0 * * * ?)
     */
    @Scheduled(cron = "${scheduler.sla-report.cron:0 0 * * * ?}")
    public void generateHourlySlaReport() {
        try {
            log.info("Generating hourly SLA report...");
            
            List<SlaViolationResponse> violations = slaService.getSlaViolations();
            List<SlaAtRiskResponse> atRisk = slaService.getAtRiskConversations(10);
            
            log.info("📊 SLA Hourly Report: {} violations, {} at-risk conversations",
                    violations.size(), atRisk.size());

        } catch (Exception e) {
            log.error("❌ Error generating SLA report: {}", e.getMessage(), e);
        }
    }
}
