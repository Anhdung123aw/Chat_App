package com.example.chatcore.exception;

public class AgentNotFoundException extends RuntimeException {
    public AgentNotFoundException(String message) {
        super(message);
    }

    public AgentNotFoundException(String agentId, String reason) {
        super(String.format("Agent %s không tồn tại: %s", agentId, reason));
    }
}
