package com.example.chatcore.service;

import com.example.chatcore.entity.ChatAlertConfigEntity;
import com.example.chatcore.enums.AgentStatus;
import com.example.chatcore.enums.AlertType;
import com.example.chatcore.enums.ConversationStatus;
import com.example.chatcore.repository.ChatAgentRepository;
import com.example.chatcore.repository.ChatAlertConfigRepository;
import com.example.chatcore.repository.ChatConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service kiểm tra và gửi cảnh báo vận hành
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final ChatAlertConfigRepository alertConfigRepository;
    private final ChatConversationRepository conversationRepository;
    private final ChatAgentRepository agentRepository;

    /**
     * Check queue depth alert
     * Alert nếu số conversation WAITING > threshold
     */
    public List<AlertMessage> checkQueueDepthAlert() {
        List<AlertMessage> alerts = new ArrayList<>();

        ChatAlertConfigEntity config = alertConfigRepository.findByAlertTypeAndEnabled(AlertType.QUEUE_DEPTH, true)
                .orElse(null);
        
        if (config == null) {
            return alerts;
        }

        long queueDepth = conversationRepository.countByStatus(ConversationStatus.NEW);

        if (queueDepth > config.getThreshold()) {
            String message = String.format(
                    "⚠️ QUEUE_DEPTH ALERT: %d conversations waiting (threshold: %d)",
                    queueDepth, config.getThreshold()
            );
            
            AlertMessage alert = new AlertMessage(
                    AlertType.QUEUE_DEPTH,
                    message,
                    queueDepth,
                    config.getThreshold(),
                    LocalDateTime.now()
            );

            alerts.add(alert);
            log.warn(message);
            
            // Send alert (Slack/Email)
            sendAlert(config, alert);
        }

        return alerts;
    }

    /**
     * Check SLA breach alert
     * Alert nếu số conversation vi phạm SLA > threshold
     */
    public List<AlertMessage> checkSlaBreachAlert() {
        List<AlertMessage> alerts = new ArrayList<>();

        ChatAlertConfigEntity config = alertConfigRepository.findByAlertTypeAndEnabled(AlertType.SLA_BREACH, true)
                .orElse(null);
        
        if (config == null) {
            return alerts;
        }

        LocalDateTime now = LocalDateTime.now();
        long slaBreachCount = conversationRepository.findSlaViolations(now).size();

        if (slaBreachCount > config.getThreshold()) {
            String message = String.format(
                    "⚠️ SLA_BREACH ALERT: %d conversations breached SLA (threshold: %d)",
                    slaBreachCount, config.getThreshold()
            );

            AlertMessage alert = new AlertMessage(
                    AlertType.SLA_BREACH,
                    message,
                    slaBreachCount,
                    config.getThreshold(),
                    LocalDateTime.now()
            );

            alerts.add(alert);
            log.warn(message);
            
            sendAlert(config, alert);
        }

        return alerts;
    }

    /**
     * Check low agent capacity alert
     * Alert nếu số agents ONLINE < threshold
     */
    public List<AlertMessage> checkLowAgentCapacityAlert() {
        List<AlertMessage> alerts = new ArrayList<>();

        ChatAlertConfigEntity config = alertConfigRepository.findByAlertTypeAndEnabled(AlertType.LOW_AGENT_CAPACITY, true)
                .orElse(null);
        
        if (config == null) {
            return alerts;
        }

        long onlineAgents = agentRepository.countByStatus(AgentStatus.ONLINE);

        if (onlineAgents < config.getThreshold()) {
            String message = String.format(
                    "⚠️ LOW_AGENT_CAPACITY ALERT: Only %d agents online (threshold: %d)",
                    onlineAgents, config.getThreshold()
            );

            AlertMessage alert = new AlertMessage(
                    AlertType.LOW_AGENT_CAPACITY,
                    message,
                    onlineAgents,
                    config.getThreshold(),
                    LocalDateTime.now()
            );

            alerts.add(alert);
            log.warn(message);
            
            sendAlert(config, alert);
        }

        return alerts;
    }

    /**
     * Check high wait time alert
     * Alert nếu có conversation chờ quá lâu (> threshold seconds)
     */
    public List<AlertMessage> checkHighWaitTimeAlert() {
        List<AlertMessage> alerts = new ArrayList<>();

        ChatAlertConfigEntity config = alertConfigRepository.findByAlertTypeAndEnabled(AlertType.HIGH_WAIT_TIME, true)
                .orElse(null);
        
        if (config == null) {
            return alerts;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusSeconds(config.getThreshold());

        long highWaitCount = conversationRepository.countByStatusAndCreatedAtBefore(
                ConversationStatus.NEW, 
                threshold
        );

        if (highWaitCount > 0) {
            String message = String.format(
                    "⚠️ HIGH_WAIT_TIME ALERT: %d conversations waiting > %d seconds",
                    highWaitCount, config.getThreshold()
            );

            AlertMessage alert = new AlertMessage(
                    AlertType.HIGH_WAIT_TIME,
                    message,
                    highWaitCount,
                    config.getThreshold(),
                    LocalDateTime.now()
            );

            alerts.add(alert);
            log.warn(message);
            
            sendAlert(config, alert);
        }

        return alerts;
    }

    /**
     * Check all alerts
     */
    public List<AlertMessage> checkAllAlerts() {
        log.info("Checking all alerts");

        List<AlertMessage> allAlerts = new ArrayList<>();
        
        allAlerts.addAll(checkQueueDepthAlert());
        allAlerts.addAll(checkSlaBreachAlert());
        allAlerts.addAll(checkLowAgentCapacityAlert());
        allAlerts.addAll(checkHighWaitTimeAlert());

        log.info("Total alerts triggered: {}", allAlerts.size());
        return allAlerts;
    }

    /**
     * Send alert to configured channel (Slack/Email)
     */
    private void sendAlert(ChatAlertConfigEntity config, AlertMessage alert) {
        try {
            // TODO: Parse notificationChannels JSON and send to appropriate channels
            log.info("Would send alert via channels {}: {}", config.getNotificationChannels(), alert.message());
        } catch (Exception e) {
            log.error("Failed to send alert: {}", e.getMessage(), e);
        }
    }

    /**
     * Send Slack webhook alert
     */
    private void sendSlackAlert(String webhookUrl, AlertMessage alert) {
        // TODO: Implement Slack webhook integration
        log.info("Would send Slack alert to {}: {}", webhookUrl, alert.message());
        
        // Example implementation (requires HTTP client):
        // String payload = String.format("{\"text\": \"%s\"}", alert.message());
        // httpClient.post(webhookUrl, payload);
    }

    /**
     * Send Email alert
     */
    private void sendEmailAlert(String recipients, AlertMessage alert) {
        // TODO: Implement Email integration
        log.info("Would send Email alert to {}: {}", recipients, alert.message());
        
        // Example implementation (requires JavaMailSender):
        // mailSender.send(recipients, "Chat Alert", alert.message());
    }

    /**
     * Alert Message record
     */
    public record AlertMessage(
            AlertType alertType,
            String message,
            long currentValue,
            int threshold,
            LocalDateTime timestamp
    ) {}
}
