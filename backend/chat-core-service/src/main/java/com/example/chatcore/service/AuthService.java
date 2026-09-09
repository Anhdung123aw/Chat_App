package com.example.chatcore.service;

import com.example.chatcore.dto.AuthDTO.*;
import com.example.chatcore.dto.ContextDTO;
import com.example.chatcore.entity.ChatConversationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Authentication Service
 * Handles bootstrap, webview token, and token exchange
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final ConversationService conversationService;

    /**
     * Bootstrap chat session
     * Creates or retrieves existing conversation for user
     */
    @Transactional
    public BootstrapResponse bootstrap(BootstrapRequest request) {
        log.info("Bootstrap chat session for user: {}", request.userId());

        // Create new conversation
        ChatConversationEntity conversation = conversationService.createConversation(
                request.userId(),
                request.merchantId(),
                request.topicCode(),
                request.context()
        );

        // Generate session token (simple implementation, use JWT in production)
        String sessionToken = generateSessionToken(request.userId(), conversation.getConversationId());

        return new BootstrapResponse(
                conversation.getConversationId(),
                conversation.getUserId(),
                conversation.getMerchantId(),
                conversation.getStatus().name(),
                sessionToken,
                3600L // 1 hour
        );
    }

    /**
     * Generate webview token for embedding chat in webview
     */
    public WebviewTokenResponse generateWebviewToken(WebviewTokenRequest request) {
        log.info("Generate webview token for user: {}, conversation: {}", 
                request.userId(), request.conversationId());

        // Generate temporary token (simple implementation)
        String token = generateTempToken(request.userId(), request.conversationId());
        String webviewUrl = String.format("/webview/chat?token=%s", token);

        return new WebviewTokenResponse(
                token,
                "Bearer",
                300L, // 5 minutes
                webviewUrl
        );
    }

    /**
     * Exchange temporary token for access token
     */
    public TokenExchangeResponse exchangeToken(TokenExchangeRequest request) {
        log.info("Exchange token, grant type: {}", request.grantType());

        // Validate temp token (simplified - use proper JWT validation in production)
        validateTempToken(request.tempToken());

        // Generate access token and refresh token
        String accessToken = generateAccessToken();
        String refreshToken = generateRefreshToken();

        return new TokenExchangeResponse(
                accessToken,
                "Bearer",
                3600L, // 1 hour
                refreshToken,
                "chat.read chat.write"
        );
    }

    // ============= Private Helper Methods =============

    private String generateSessionToken(String userId, String conversationId) {
        // Simple implementation - use proper JWT in production
        String payload = String.format("%s:%s:%d", userId, conversationId, Instant.now().toEpochMilli());
        return Base64.getUrlEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private String generateTempToken(String userId, String conversationId) {
        // Temporary token with short expiry
        String payload = String.format("temp:%s:%s:%d", userId, conversationId, Instant.now().toEpochMilli());
        return Base64.getUrlEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private String generateAccessToken() {
        // Simple UUID-based token - use JWT in production
        return "access_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String generateRefreshToken() {
        return "refresh_" + UUID.randomUUID().toString().replace("-", "");
    }

    private void validateTempToken(String tempToken) {
        // Simplified validation - decode and check expiry
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(tempToken);
            String payload = new String(decoded, StandardCharsets.UTF_8);
            
            if (!payload.startsWith("temp:")) {
                throw new IllegalArgumentException("Invalid token format");
            }

            // Extract timestamp and validate expiry (5 minutes)
            String[] parts = payload.split(":");
            long timestamp = Long.parseLong(parts[parts.length - 1]);
            long now = Instant.now().toEpochMilli();
            
            if (now - timestamp > 300000) { // 5 minutes in milliseconds
                throw new IllegalArgumentException("Token expired");
            }
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid or expired token");
        }
    }
}
