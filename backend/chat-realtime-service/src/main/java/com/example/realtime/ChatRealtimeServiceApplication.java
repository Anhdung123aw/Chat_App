package com.example.realtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Chat Realtime Service - WebSocket Gateway
 * 
 * Responsibilities:
 * - WebSocket connection management
 * - Real-time message delivery
 * - Typing indicators
 * - Presence tracking
 * - Redis Pub/Sub message distribution
 * - Kafka event consumption
 */
@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableScheduling
public class ChatRealtimeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatRealtimeServiceApplication.class, args);
    }
}
