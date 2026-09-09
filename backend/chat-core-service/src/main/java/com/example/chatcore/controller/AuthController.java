package com.example.chatcore.controller;

import com.example.chatcore.dto.AuthDTO.*;
import com.example.chatcore.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles bootstrap, webview token, and token exchange
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/chat/bootstrap
     * Initialize chat session and return conversation ID
     */
    @PostMapping("/chat/bootstrap")
    @ResponseStatus(HttpStatus.OK)
    public BootstrapResponse bootstrap(@Valid @RequestBody BootstrapRequest request) {
        return authService.bootstrap(request);
    }

    /**
     * POST /api/v1/auth/webview-token
     * Generate token for webview authentication
     */
    @PostMapping("/auth/webview-token")
    @ResponseStatus(HttpStatus.OK)
    public WebviewTokenResponse getWebviewToken(@Valid @RequestBody WebviewTokenRequest request) {
        return authService.generateWebviewToken(request);
    }

    /**
     * POST /api/v1/auth/exchange
     * Exchange temporary token for access token
     */
    @PostMapping("/auth/exchange")
    @ResponseStatus(HttpStatus.OK)
    public TokenExchangeResponse exchangeToken(@Valid @RequestBody TokenExchangeRequest request) {
        return authService.exchangeToken(request);
    }
}
