package com.example.chatrealtime.handler;

import com.example.chatrealtime.session.WebSocketSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.addSessionToUser(userId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : null;

            if ("SUBSCRIBE".equals(type) && jsonNode.has("conversationId")) {
                String conversationId = jsonNode.get("conversationId").asText();
                sessionManager.subscribeConversation(conversationId, session);
            } else if ("PING".equals(type)) {
                session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
            }
            // Other events like TYPING could be handled here and broadcasted directly
        } catch (Exception e) {
            log.error("Error processing websocket message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.removeSession(session);
    }
}
