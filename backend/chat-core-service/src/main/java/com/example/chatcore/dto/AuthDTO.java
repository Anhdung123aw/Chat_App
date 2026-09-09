package com.example.chatcore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthDTO {

    /**
     * Bootstrap Request - Initialize chat session
     */
    public record BootstrapRequest(
            @NotBlank(message = "User ID is required") String userId,
            String merchantId,
            String topicCode,
            @Valid ContextDTO.ChatContextRequest context
    ) {}

    /**
     * Bootstrap Response - Return conversation ID and session info
     */
    public record BootstrapResponse(
            String conversationId,
            String userId,
            String merchantId,
            String status,
            String sessionToken,
            Long expiresIn
    ) {}

    /**
     * Webview Token Request
     */
    public record WebviewTokenRequest(
            @NotBlank(message = "User ID is required") String userId,
            @NotBlank(message = "Conversation ID is required") String conversationId
    ) {}

    /**
     * Webview Token Response
     */
    public record WebviewTokenResponse(
            String token,
            String tokenType,
            Long expiresIn,
            String webviewUrl
    ) {}

    /**
     * Token Exchange Request
     */
    public record TokenExchangeRequest(
            @NotBlank(message = "Temporary token is required") String tempToken,
            @NotBlank(message = "Grant type is required") String grantType
    ) {}

    /**
     * Token Exchange Response
     */
    public record TokenExchangeResponse(
            String accessToken,
            String tokenType,
            Long expiresIn,
            String refreshToken,
            String scope
    ) {}
}
