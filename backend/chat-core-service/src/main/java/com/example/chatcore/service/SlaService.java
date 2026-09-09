package com.example.chatcore.service;

import com.example.chatcore.dto.SlaDTO.*;
import com.example.chatcore.entity.ChatConversationEntity;
import com.example.chatcore.entity.ChatSlaConfigEntity;
import com.example.chatcore.exception.SlaConfigNotFoundException;
import com.example.chatcore.repository.ChatConversationRepository;
import com.example.chatcore.repository.ChatSlaConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service quản lý SLA (Service Level Agreement)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlaService {

    private final ChatSlaConfigRepository slaConfigRepository;
    private final ChatConversationRepository conversationRepository;

    /**
     * Tạo SLA config mới
     */
    @Transactional
    public SlaConfigResponse createSlaConfig(CreateSlaConfigRequest request) {
        log.info("Creating SLA config for topicCode: {}", request.topic());

        ChatSlaConfigEntity entity = ChatSlaConfigEntity.builder()
                .configId(UUID.randomUUID().toString())
                .topicCode(request.topic())
                .firstResponseTimeSeconds(request.firstResponseTimeSeconds())
                .resolutionTimeSeconds(request.resolutionTimeSeconds())
                .enabled(true)
                .build();

        slaConfigRepository.save(entity);
        log.info("Created SLA config: {}", entity.getConfigId());

        return SlaConfigResponse.from(entity);
    }

    /**
     * Get SLA config by topicCode
     */
    public SlaConfigResponse getSlaConfigByTopic(String topicCode) {
        ChatSlaConfigEntity entity = slaConfigRepository.findByTopicCode(topicCode)
                .orElseThrow(() -> new SlaConfigNotFoundException("SLA config not found for topicCode: " + topicCode));
        return SlaConfigResponse.from(entity);
    }

    /**
     * Get default SLA config (topicCode = "default")
     */
    public SlaConfigResponse getDefaultSlaConfig() {
        ChatSlaConfigEntity entity = slaConfigRepository.findByTopicCode("default")
                .orElseThrow(() -> new SlaConfigNotFoundException("Default SLA config not found"));
        return SlaConfigResponse.from(entity);
    }

    /**
     * List all SLA configs
     */
    public List<SlaConfigResponse> listAllSlaConfigs() {
        List<ChatSlaConfigEntity> entities = slaConfigRepository.findAll();
        return entities.stream()
                .map(SlaConfigResponse::from)
                .toList();
    }

    /**
     * Tính SLA deadline cho conversation khi tạo mới
     * Formula: SLA_DEADLINE = CREATED_AT + firstResponseTimeSeconds
     */
    public LocalDateTime calculateSlaDeadline(String topicCode, LocalDateTime createdAt) {
        try {
            ChatSlaConfigEntity slaConfig = slaConfigRepository.findByTopicCode(topicCode)
                    .orElseGet(() -> {
                        log.warn("No SLA config found for topicCode: {}, using default", topicCode);
                        return slaConfigRepository.findByTopicCode("default")
                                .orElse(null);
                    });

            if (slaConfig == null) {
                log.error("No default SLA config found");
                // Fallback: 1 hour
                return createdAt.plusSeconds(3600);
            }

            return createdAt.plusSeconds(slaConfig.getFirstResponseTimeSeconds());
        } catch (Exception e) {
            log.error("Error calculating SLA deadline: {}", e.getMessage());
            // Fallback: 1 hour
            return createdAt.plusSeconds(3600);
        }
    }

    /**
     * Check xem conversation có vi phạm SLA không
     * Vi phạm nếu: NOW > SLA_DEADLINE && FIRST_RESPONSE_AT == null
     */
    public boolean checkSlaViolation(String conversationId) {
        ChatConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));

        // Nếu đã có first response thì không vi phạm
        if (conversation.getFirstResponseAt() != null) {
            return false;
        }

        // Nếu chưa có SLA deadline thì không tính vi phạm
        if (conversation.getSlaDeadline() == null) {
            return false;
        }

        // Vi phạm nếu NOW > SLA_DEADLINE
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(conversation.getSlaDeadline());
    }

    /**
     * Get danh sách conversations vi phạm SLA
     */
    public List<SlaViolationResponse> getSlaViolations() {
        LocalDateTime now = LocalDateTime.now();
        List<ChatConversationEntity> violations = conversationRepository.findSlaViolations(now);

        return violations.stream()
                .map(conv -> {
                    long violationSeconds = java.time.Duration.between(conv.getSlaDeadline(), now).getSeconds();
                    return new SlaViolationResponse(
                            conv.getConversationId(),
                            conv.getTopicCode(),
                            conv.getSlaDeadline(),
                            now,
                            violationSeconds,
                            conv.getAssignedAgent()
                    );
                })
                .toList();
    }

    /**
     * Get conversations at risk (sắp vi phạm SLA trong 5 phút)
     */
    public List<SlaAtRiskResponse> getAtRiskConversations(int minutesThreshold) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(minutesThreshold);

        List<ChatConversationEntity> atRisk = conversationRepository.findSlaAtRisk(now, threshold);

        return atRisk.stream()
                .map(conv -> {
                    long remainingSeconds = java.time.Duration.between(now, conv.getSlaDeadline()).getSeconds();
                    return new SlaAtRiskResponse(
                            conv.getConversationId(),
                            conv.getTopicCode(),
                            conv.getSlaDeadline(),
                            remainingSeconds,
                            conv.getAssignedAgent()
                    );
                })
                .toList();
    }

    /**
     * Update SLA config
     */
    @Transactional
    public SlaConfigResponse updateSlaConfig(String configId, UpdateSlaConfigRequest request) {
        log.info("Updating SLA config: {}", configId);

        ChatSlaConfigEntity entity = slaConfigRepository.findById(configId)
                .orElseThrow(() -> new SlaConfigNotFoundException(configId));

        if (request.firstResponseTimeSeconds() != null) {
            entity.setFirstResponseTimeSeconds(request.firstResponseTimeSeconds());
        }
        if (request.resolutionTimeSeconds() != null) {
            entity.setResolutionTimeSeconds(request.resolutionTimeSeconds());
        }

        slaConfigRepository.save(entity);
        return SlaConfigResponse.from(entity);
    }

    /**
     * Get SLA statistics
     */
    public SlaStatisticsResponse getSlaStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();

        long totalConversations = conversationRepository.countCreatedBetween(startDate, endDate);
        long violatedConversations = conversationRepository.findSlaViolations(now).stream()
                .filter(conv -> conv.getCreatedAt().isAfter(startDate) && conv.getCreatedAt().isBefore(endDate))
                .count();
        long atRiskConversations = conversationRepository.findSlaAtRisk(now, now.plusMinutes(5)).size();

        double complianceRate = totalConversations > 0 
                ? ((double) (totalConversations - violatedConversations) / totalConversations) * 100.0 
                : 100.0;

        return new SlaStatisticsResponse(
                totalConversations,
                violatedConversations,
                atRiskConversations,
                complianceRate,
                startDate,
                endDate
        );
    }
}
