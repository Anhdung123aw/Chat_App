package com.example.chatcore.service;

import com.example.chatcore.entity.ChatOutboxEntity;
import com.example.chatcore.repository.ChatOutboxRepository;
import com.example.chatcore.enums.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final ChatOutboxRepository outboxRepository;
    private final KafkaTemplate kafkaTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPending() {
        List<ChatOutboxEntity> events = outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        if (events.isEmpty()) {
            return;
        }
        for (ChatOutboxEntity event : events) {
            try {
                kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload()).get();
                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getEventId(), e.getMessage());
                event.setStatus(OutboxStatus.FAILED);
            }
        }
        outboxRepository.saveAll(events);
    }
}