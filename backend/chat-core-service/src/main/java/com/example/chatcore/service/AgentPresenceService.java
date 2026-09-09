package com.example.chatcore.service;

import com.example.chatcore.dto.AgentDTO.*;
import com.example.chatcore.entity.ChatAgentEntity;
import com.example.chatcore.enums.AgentStatus;
import com.example.chatcore.exception.AgentNotFoundException;
import com.example.chatcore.repository.ChatAgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service quản lý Agent Presence (trạng thái online/offline)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentPresenceService {

    private final ChatAgentRepository agentRepository;

    /**
     * Tạo agent mới
     */
    @Transactional
    public AgentResponse createAgent(CreateAgentRequest request) {
        log.info("Creating agent: {}", request.name());

        ChatAgentEntity entity = ChatAgentEntity.builder()
                .agentId(UUID.randomUUID().toString())
                .agentName(request.name())
                .email(request.email())
                .status(AgentStatus.OFFLINE)
                .skills(request.skills())
                .maxConcurrentChats(request.maxConcurrentChats() != null ? request.maxConcurrentChats() : 5)
                .currentChatCount(0)
                .lastActiveAt(LocalDateTime.now())
                .build();

        agentRepository.save(entity);
        log.info("Created agent: {}", entity.getAgentId());

        return AgentResponse.from(entity);
    }

    /**
     * Update agent status (ONLINE, OFFLINE, BUSY, AWAY)
     */
    @Transactional
    public AgentResponse updateAgentStatus(String agentId, AgentStatus newStatus) {
        log.info("Updating agent {} status to {}", agentId, newStatus);

        ChatAgentEntity agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));

        agent.setStatus(newStatus);
        agent.setLastActiveAt(LocalDateTime.now());

        agentRepository.save(agent);
        return AgentResponse.from(agent);
    }

    /**
     * Track agent heartbeat (gọi mỗi 30s để detect agent còn online không)
     */
    @Transactional
    public void trackHeartbeat(String agentId) {
        ChatAgentEntity agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));

        agent.setLastActiveAt(LocalDateTime.now());
        agentRepository.save(agent);
        
        log.debug("Heartbeat tracked for agent: {}", agentId);
    }

    /**
     * Increment current chat count khi assign conversation
     */
    @Transactional
    public void incrementChatCount(String agentId) {
        ChatAgentEntity agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));

        agent.setCurrentChatCount(agent.getCurrentChatCount() + 1);
        agentRepository.save(agent);
        
        log.info("Agent {} chat count increased to {}", agentId, agent.getCurrentChatCount());
    }

    /**
     * Decrement current chat count khi close conversation
     */
    @Transactional
    public void decrementChatCount(String agentId) {
        ChatAgentEntity agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));

        if (agent.getCurrentChatCount() > 0) {
            agent.setCurrentChatCount(agent.getCurrentChatCount() - 1);
            agentRepository.save(agent);
            
            log.info("Agent {} chat count decreased to {}", agentId, agent.getCurrentChatCount());
        }
    }

    /**
     * Get danh sách agents online
     */
    public List<AgentResponse> getOnlineAgents() {
        List<ChatAgentEntity> agents = agentRepository.findByStatus(AgentStatus.ONLINE);
        return agents.stream()
                .map(AgentResponse::from)
                .toList();
    }

    /**
     * Get danh sách available agents (ONLINE + còn capacity)
     */
    public List<AgentResponse> getAvailableAgents() {
        List<ChatAgentEntity> agents = agentRepository.findAvailableAgents();
        return agents.stream()
                .map(AgentResponse::from)
                .toList();
    }

    /**
     * Get agents by skills (simplified version - exact match only)
     */
    public List<AgentResponse> getAgentsBySkills(String skills) {
        // Fallback: Get all agents and filter manually
        List<ChatAgentEntity> allAgents = agentRepository.findAll();
        List<ChatAgentEntity> matchedAgents = allAgents.stream()
                .filter(agent -> agent.getSkills() != null && agent.getSkills().contains(skills))
                .toList();
        
        return matchedAgents.stream()
                .map(AgentResponse::from)
                .toList();
    }

    /**
     * Get agent by ID
     */
    public AgentResponse getAgentById(String agentId) {
        ChatAgentEntity agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));
        return AgentResponse.from(agent);
    }

    /**
     * Update agent capacity (max concurrent chats)
     */
    @Transactional
    public AgentResponse updateAgentCapacity(String agentId, Integer maxConcurrentChats) {
        log.info("Updating agent {} capacity to {}", agentId, maxConcurrentChats);

        ChatAgentEntity agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));

        agent.setMaxConcurrentChats(maxConcurrentChats);
        agentRepository.save(agent);

        return AgentResponse.from(agent);
    }

    /**
     * Update agent skills
     */
    @Transactional
    public AgentResponse updateAgentSkills(String agentId, String skills) {
        log.info("Updating agent {} skills to: {}", agentId, skills);

        ChatAgentEntity agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));

        agent.setSkills(skills);
        agentRepository.save(agent);

        return AgentResponse.from(agent);
    }

    /**
     * Detect offline agents (lastActiveAt > 2 minutes)
     */
    @Transactional
    public void markStaleAgentsAsOffline() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);
        
        // Manual query: find agents with status=ONLINE and lastActiveAt < threshold
        List<ChatAgentEntity> allOnlineAgents = agentRepository.findByStatus(AgentStatus.ONLINE);
        List<ChatAgentEntity> staleAgents = allOnlineAgents.stream()
                .filter(agent -> agent.getLastActiveAt() != null && agent.getLastActiveAt().isBefore(threshold))
                .toList();

        for (ChatAgentEntity agent : staleAgents) {
            log.warn("Agent {} lastActiveAt stale, marking as OFFLINE", agent.getAgentId());
            agent.setStatus(AgentStatus.OFFLINE);
            agentRepository.save(agent);
        }
    }
}
