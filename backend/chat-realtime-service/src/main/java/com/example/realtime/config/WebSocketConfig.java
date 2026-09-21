package com.example.realtime.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

/**
 * WebSocket Configuration with STOMP protocol
 * 
 * STOMP Destinations:
 * - /app/* : Client sends to server
 * - /topic/* : Server broadcasts to multiple clients
 * - /queue/* : Server sends to specific user/client
 * 
 * Example flows:
 * 1. Client subscribes: /user/queue/messages (private messages)
 * 2. Client subscribes: /topic/conversation/{conversationId} (conversation messages)
 * 3. Client sends: /app/chat.send (send message to server)
 * 4. Server broadcasts: /topic/conversation/{conversationId} (new message event)
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.websocket.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:4201}")
    private List<String> allowedOrigins;

    @Value("${spring.websocket.endpoint:/ws}")
    private String websocketEndpoint;

    @Value("${spring.websocket.message-size-limit:128KB}")
    private String messageSizeLimit;

    @Value("${spring.websocket.send-time-limit:20s}")
    private String sendTimeLimit;

    @Value("${spring.websocket.send-buffer-size-limit:512KB}")
    private String sendBufferSizeLimit;

    /**
     * Configure message broker
     * - /topic for pub/sub (one-to-many)
     * - /queue for point-to-point (one-to-one)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple broker for /topic and /queue destinations
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages from clients
        registry.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations
        registry.setUserDestinationPrefix("/user");
        
        log.info("WebSocket message broker configured - application prefix: /app, broker prefixes: /topic, /queue");
    }

    /**
     * Register STOMP endpoints with SockJS fallback
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(websocketEndpoint)
                .setAllowedOrigins(allowedOrigins.toArray(new String[0]))
                .withSockJS()
                .setHeartbeatTime(25000) // 25 seconds
                .setDisconnectDelay(5000); // 5 seconds
        
        log.info("WebSocket STOMP endpoint registered: {} with origins: {}", websocketEndpoint, allowedOrigins);
    }

    /**
     * Configure WebSocket transport options
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
                .setMessageSizeLimit(parseSize(messageSizeLimit)) // Max incoming message size
                .setSendTimeLimit(parseTime(sendTimeLimit)) // Max time to send message
                .setSendBufferSizeLimit(parseSize(sendBufferSizeLimit)); // Buffer size
        
        log.info("WebSocket transport configured - messageSize: {}, sendTime: {}, bufferSize: {}", 
                 messageSizeLimit, sendTimeLimit, sendBufferSizeLimit);
    }

    /**
     * Parse size string (e.g., "128KB") to bytes
     */
    private int parseSize(String size) {
        size = size.toUpperCase().trim();
        if (size.endsWith("KB")) {
            return Integer.parseInt(size.replace("KB", "").trim()) * 1024;
        } else if (size.endsWith("MB")) {
            return Integer.parseInt(size.replace("MB", "").trim()) * 1024 * 1024;
        }
        return Integer.parseInt(size);
    }

    /**
     * Parse time string (e.g., "20s") to milliseconds
     */
    private int parseTime(String time) {
        time = time.toLowerCase().trim();
        if (time.endsWith("s")) {
            return Integer.parseInt(time.replace("s", "").trim()) * 1000;
        } else if (time.endsWith("ms")) {
            return Integer.parseInt(time.replace("ms", "").trim());
        }
        return Integer.parseInt(time);
    }
}
