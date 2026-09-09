package com.example.chatcore.service;

import com.example.chatcore.dto.ReportDTO.*;
import com.example.chatcore.entity.ChatConversationEntity;
import com.example.chatcore.enums.ConversationStatus;
import com.example.chatcore.repository.ChatConversationRepository;
import com.example.chatcore.repository.ChatMessageRepository;
import com.example.chatcore.repository.ChatRatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service tạo báo cáo và tính toán metrics vận hành
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatRatingRepository ratingRepository;

    /**
     * Tính First Response Time trung bình (FRT)
     * FRT = Thời gian từ khi conversation tạo đến khi agent reply lần đầu
     */
    public FrtReportResponse calculateFrt(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating FRT from {} to {}", startDate, endDate);

        List<ChatConversationEntity> conversations = conversationRepository
                .findByCreatedAtBetween(startDate, endDate);

        List<Long> frtSeconds = conversations.stream()
                .filter(conv -> conv.getFirstResponseAt() != null)
                .map(conv -> Duration.between(conv.getCreatedAt(), conv.getFirstResponseAt()).getSeconds())
                .toList();

        if (frtSeconds.isEmpty()) {
            return new FrtReportResponse(0.0, 0L, 0L, 0L, startDate, endDate);
        }

        double avgFrt = frtSeconds.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long minFrt = frtSeconds.stream().min(Long::compare).orElse(0L);
        long maxFrt = frtSeconds.stream().max(Long::compare).orElse(0L);

        return new FrtReportResponse(
                avgFrt,
                minFrt,
                maxFrt,
                (long) frtSeconds.size(),
                startDate,
                endDate
        );
    }

    /**
     * Tính Average Handle Time (AHT)
     * AHT = Thời gian từ khi conversation tạo đến khi CLOSED
     */
    public AhtReportResponse calculateAht(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating AHT from {} to {}", startDate, endDate);

        List<ChatConversationEntity> conversations = conversationRepository
                .findByCreatedAtBetween(startDate, endDate).stream()
                .filter(conv -> conv.getStatus() == ConversationStatus.CLOSED && conv.getClosedAt() != null)
                .toList();

        List<Long> ahtSeconds = conversations.stream()
                .map(conv -> Duration.between(conv.getCreatedAt(), conv.getClosedAt()).getSeconds())
                .toList();

        if (ahtSeconds.isEmpty()) {
            return new AhtReportResponse(0.0, 0L, 0L, 0L, startDate, endDate);
        }

        double avgAht = ahtSeconds.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long minAht = ahtSeconds.stream().min(Long::compare).orElse(0L);
        long maxAht = ahtSeconds.stream().max(Long::compare).orElse(0L);

        return new AhtReportResponse(
                avgAht,
                minAht,
                maxAht,
                (long) ahtSeconds.size(),
                startDate,
                endDate
        );
    }

    /**
     * Tính CSAT report
     */
    public CsatReportResponse calculateCsat(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating CSAT from {} to {}", startDate, endDate);

        Double avgRating = ratingRepository.calculateAverageCsatBetween(startDate, endDate);
        Long totalRatings = ratingRepository.countRatingsBetween(startDate, endDate);

        if (avgRating == null || totalRatings == 0) {
            return new CsatReportResponse(0.0, 0.0, 0L, startDate, endDate);
        }

        double csatPercentage = avgRating * 20.0;

        return new CsatReportResponse(
                avgRating,
                csatPercentage,
                totalRatings,
                startDate,
                endDate
        );
    }

    /**
     * Tính SLA breach rate
     */
    public SlaBreachReportResponse calculateSlaBreach(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating SLA breach from {} to {}", startDate, endDate);

        List<ChatConversationEntity> conversations = conversationRepository
                .findByCreatedAtBetween(startDate, endDate);

        long totalConversations = conversations.size();
        long breachedConversations = conversations.stream()
                .filter(conv -> {
                    if (conv.getSlaDeadline() == null || conv.getFirstResponseAt() != null) {
                        return false;
                    }
                    return LocalDateTime.now().isAfter(conv.getSlaDeadline());
                })
                .count();

        double breachRate = totalConversations > 0 
                ? ((double) breachedConversations / totalConversations) * 100.0 
                : 0.0;

        return new SlaBreachReportResponse(
                totalConversations,
                breachedConversations,
                breachRate,
                startDate,
                endDate
        );
    }

    /**
     * Get top topics
     */
    public TopTopicsReportResponse getTopTopics(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        log.info("Getting top {} topics from {} to {}", limit, startDate, endDate);

        List<ChatConversationEntity> conversations = conversationRepository
                .findByCreatedAtBetween(startDate, endDate);

        Map<String, Long> topicCounts = conversations.stream()
                .filter(conv -> conv.getTopicCode() != null && !conv.getTopicCode().isEmpty())
                .collect(Collectors.groupingBy(
                        ChatConversationEntity::getTopicCode,
                        Collectors.counting()
                ));

        List<TopicCount> topTopics = topicCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new TopicCount(entry.getKey(), entry.getValue()))
                .toList();

        return new TopTopicsReportResponse(
                topTopics,
                (long) conversations.size(),
                startDate,
                endDate
        );
    }

    /**
     * Get agent performance report
     */
    public AgentPerformanceReportResponse getAgentPerformance(String agentId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting agent {} performance from {} to {}", agentId, startDate, endDate);

        List<ChatConversationEntity> conversations = conversationRepository
                .findByAssignedAgentAndCreatedAtBetween(agentId, startDate, endDate);

        long totalConversations = conversations.size();
        long closedConversations = conversations.stream()
                .filter(conv -> conv.getStatus() == ConversationStatus.CLOSED)
                .count();

        // Calculate FRT for this agent
        List<Long> frtSeconds = conversations.stream()
                .filter(conv -> conv.getFirstResponseAt() != null)
                .map(conv -> Duration.between(conv.getCreatedAt(), conv.getFirstResponseAt()).getSeconds())
                .toList();

        Double avgFrt = frtSeconds.isEmpty() ? 0.0 : frtSeconds.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        // Calculate AHT for this agent
        List<Long> ahtSeconds = conversations.stream()
                .filter(conv -> conv.getStatus() == ConversationStatus.CLOSED && conv.getClosedAt() != null)
                .map(conv -> Duration.between(conv.getCreatedAt(), conv.getClosedAt()).getSeconds())
                .toList();

        Double avgAht = ahtSeconds.isEmpty() ? 0.0 : ahtSeconds.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        // CSAT for this agent's conversations
        List<String> conversationIds = conversations.stream()
                .map(ChatConversationEntity::getConversationId)
                .toList();

        Double avgCsat = conversationIds.isEmpty() ? 0.0 :
                ratingRepository.calculateAverageCsatForConversations(conversationIds);

        return new AgentPerformanceReportResponse(
                agentId,
                totalConversations,
                closedConversations,
                avgFrt,
                avgAht,
                avgCsat != null ? avgCsat : 0.0,
                startDate,
                endDate
        );
    }

    /**
     * Get backlog report (số conversation đang chờ xử lý)
     */
    public BacklogReportResponse getBacklogReport() {
        log.info("Getting backlog report");

        long waitingCount = conversationRepository.countByStatus(ConversationStatus.NEW);
        long activeCount = conversationRepository.countByStatus(ConversationStatus.IN_PROGRESS);

        List<ChatConversationEntity> oldestWaiting = conversationRepository
                .findTop10ByStatusOrderByCreatedAtAsc(ConversationStatus.NEW);

        List<BacklogConversation> oldestConversations = oldestWaiting.stream()
                .map(conv -> {
                    long waitingSeconds = Duration.between(conv.getCreatedAt(), LocalDateTime.now()).getSeconds();
                    return new BacklogConversation(
                            conv.getConversationId(),
                            conv.getTopicCode(),
                            conv.getCreatedAt(),
                            waitingSeconds
                    );
                })
                .toList();

        return new BacklogReportResponse(
                waitingCount,
                activeCount,
                waitingCount + activeCount,
                oldestConversations
        );
    }

    /**
     * Get comprehensive dashboard report
     */
    public DashboardReportResponse getDashboardReport(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting dashboard report from {} to {}", startDate, endDate);

        FrtReportResponse frt = calculateFrt(startDate, endDate);
        AhtReportResponse aht = calculateAht(startDate, endDate);
        CsatReportResponse csat = calculateCsat(startDate, endDate);
        SlaBreachReportResponse slaBreach = calculateSlaBreach(startDate, endDate);
        TopTopicsReportResponse topTopics = getTopTopics(startDate, endDate, 5);
        BacklogReportResponse backlog = getBacklogReport();

        return new DashboardReportResponse(
                frt,
                aht,
                csat,
                slaBreach,
                topTopics,
                backlog,
                startDate,
                endDate
        );
    }
}
