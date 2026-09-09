package com.example.chatrealtime.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token != null && !token.isEmpty()) {
                // In MVP Phase 1: simple mock validation
                // e.g. token "agent1" -> userId = "agent1", senderType = "AGENT"
                // token "user1" -> userId = "user1", senderType = "USER"
                
                String userId = token; // MVP mock: token is the userId
                String senderType = token.startsWith("agent") ? "AGENT" : "USER";
                
                attributes.put("userId", userId);
                attributes.put("senderType", senderType);
                return true;
            }
        }
        return false; // Reject if no token
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Do nothing
    }
}
