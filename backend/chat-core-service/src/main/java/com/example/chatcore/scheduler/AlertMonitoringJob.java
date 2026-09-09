package com.example.chatcore.scheduler;

import com.example.chatcore.service.AlertService;
import com.example.chatcore.service.AgentPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled Job - Alert Monitoring
 * Chạy mỗi 5 phút để check alerts: queue depth, SLA breach, agent capacity, wait time
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "scheduler.alert-monitoring.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AlertMonitoringJob {

    private final AlertService alertService;
    private final AgentPresenceService agentPresenceService;

    /**
     * Check all alerts mỗi 5 phút
     * Cron: every 5 minutes (0 star/5 * * * ?)
     */
    @Scheduled(cron = "${scheduler.alert-monitoring.cron:0 0/5 * * * ?}")
    public void checkAllAlerts() {
        try {
            log.debug("Running alert monitoring job...");

            List<AlertService.AlertMessage> alerts = alertService.checkAllAlerts();

            if (!alerts.isEmpty()) {
                log.warn("⚠️ Triggered {} alerts", alerts.size());
                alerts.forEach(alert ->
                    log.warn("  - {}: {} (current: {}, threshold: {})",
                            alert.alertType(), alert.message(), 
                            alert.currentValue(), alert.threshold())
                );
            } else {
                log.debug("✅ No alerts triggered");
            }

            log.debug("Alert monitoring job completed. Total alerts: {}", alerts.size());

        } catch (Exception e) {
            log.error("❌ Error in alert monitoring job: {}", e.getMessage(), e);
        }
    }

    /**
     * Mark stale agents as offline mỗi 2 phút
     * Agents không heartbeat > 2 phút sẽ bị đánh dấu OFFLINE
     * Cron: every 2 minutes (0 star/2 * * * ?)
     */
    @Scheduled(cron = "${scheduler.agent-heartbeat-check.cron:0 0/2 * * * ?}")
    public void checkStaleAgents() {
        try {
            log.debug("Checking for stale agents...");
            
            agentPresenceService.markStaleAgentsAsOffline();
            
            log.debug("Stale agent check completed");

        } catch (Exception e) {
            log.error("❌ Error checking stale agents: {}", e.getMessage(), e);
        }
    }

    /**
     * Optional: Generate alert summary report mỗi giờ
     * Cron: at minute 15 of every hour (0 15 * * * ?)
     */
    @Scheduled(cron = "${scheduler.alert-report.cron:0 15 * * * ?}")
    public void generateHourlyAlertReport() {
        try {
            log.info("Generating hourly alert report...");
            
            List<AlertService.AlertMessage> alerts = alertService.checkAllAlerts();
            
            // Count by type
            long queueDepthAlerts = alerts.stream()
                    .filter(a -> a.alertType().name().equals("QUEUE_DEPTH"))
                    .count();
            long slaBreachAlerts = alerts.stream()
                    .filter(a -> a.alertType().name().equals("SLA_BREACH"))
                    .count();
            long capacityAlerts = alerts.stream()
                    .filter(a -> a.alertType().name().equals("LOW_AGENT_CAPACITY"))
                    .count();
            long waitTimeAlerts = alerts.stream()
                    .filter(a -> a.alertType().name().equals("HIGH_WAIT_TIME"))
                    .count();
            
            log.info("📊 Alert Hourly Report: {} total alerts (Queue: {}, SLA: {}, Capacity: {}, WaitTime: {})",
                    alerts.size(), queueDepthAlerts, slaBreachAlerts, capacityAlerts, waitTimeAlerts);

        } catch (Exception e) {
            log.error("❌ Error generating alert report: {}", e.getMessage(), e);
        }
    }
}
