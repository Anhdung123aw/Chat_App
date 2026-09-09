package com.example.chatcore.service;

import com.example.chatcore.entity.ChatAgentEntity;
import com.example.chatcore.entity.ChatConversationEntity;
import com.example.chatcore.enums.ConversationStatus;
import com.example.chatcore.exception.AgentNotFoundException;
import com.example.chatcore.repository.ChatAgentRepository;
import com.example.chatcore.repository.ChatConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service auto assignment - Tự động phân công conversation cho agent
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutoAssignmentService {

    private final ChatAgentRepository agentRepository;
    private final ChatConversationRepository conversationRepository;
    private final AgentPresenceService agentPresenceService;

    /**
     * Tự động assign conversation cho agent phù hợp nhất
     */
    @Transactional
    public boolean autoAssignConversation(String conversationId) {
        log.info("Auto assigning conversation: {}", conversationId);

        ChatConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));

        // Nếu đã assigned rồi thì skip
        if (conversation.getAssignedAgent() != null) {
            log.info("Conversation {} already assigned to agent {}", conversationId, conversation.getAssignedAgent());
            return false;
        }

        // Tìm agent phù hợp nhất
        Optional<ChatAgentEntity> bestAgent = findBestAgent(conversation);

        if (bestAgent.isEmpty()) {
            log.warn("No available agent found for conversation: {}", conversationId);
            return false;
        }

        ChatAgentEntity agent = bestAgent.get();

        // Assign conversation
        conversation.setAssignedAgent(agent.getAgentId());
        conversation.setStatus(ConversationStatus.IN_PROGRESS);
        conversationRepository.save(conversation);

        // Increment agent's chat count
        agentPresenceService.incrementChatCount(agent.getAgentId());

        log.info("Conversation {} assigned to agent {} ({})", conversationId, agent.getAgentId(), agent.getAgentName());
        return true;
    }

    /**
     * Tìm agent phù hợp nhất dựa trên:
     * 1. Skills matching (nếu conversation có topic)
     * 2. Capacity (agent còn slot trống)
     * 3. Load balancing (ưu tiên agent có ít conversation nhất)
     */
    public Optional<ChatAgentEntity> findBestAgent(ChatConversationEntity conversation) {
        String topicCode = conversation.getTopicCode();

        // Step 1: Get available agents (ONLINE + có capacity)
        List<ChatAgentEntity> availableAgents = agentRepository.findAvailableAgents();

        if (availableAgents.isEmpty()) {
            log.warn("No available agents");
            return Optional.empty();
        }

        // Step 2: Filter by skills nếu conversation có topicCode
        List<ChatAgentEntity> matchedAgents = availableAgents;
        if (topicCode != null && !topicCode.isEmpty()) {
            matchedAgents = availableAgents.stream()
                    .filter(agent -> agent.getSkills() != null && agent.getSkills().contains(topicCode))
                    .toList();

            // Fallback: Nếu không có agent match skills, dùng tất cả available agents
            if (matchedAgents.isEmpty()) {
                log.info("No skill-matched agents for topicCode: {}, using all available agents", topicCode);
                matchedAgents = availableAgents;
            } else {
                log.info("Found {} skill-matched agents for topicCode: {}", matchedAgents.size(), topicCode);
            }
        }

        // Step 3: Load balancing - chọn agent có currentChatCount thấp nhất
        return matchedAgents.stream()
                .min(Comparator.comparingInt(ChatAgentEntity::getCurrentChatCount));
    }

    /**
     * Reassign conversation sang agent khác
     */
    @Transactional
    public boolean reassignConversation(String conversationId, String newAgentId) {
        log.info("Reassigning conversation {} to agent {}", conversationId, newAgentId);

        ChatConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));

        ChatAgentEntity newAgent = agentRepository.findById(newAgentId)
                .orElseThrow(() -> new AgentNotFoundException(newAgentId));

        // Check new agent có capacity không
        if (!newAgent.canAcceptMoreChats()) {
            log.warn("Agent {} cannot accept more chats", newAgentId);
            return false;
        }

        String oldAgentId = conversation.getAssignedAgent();

        // Decrement old agent's count
        if (oldAgentId != null) {
            agentPresenceService.decrementChatCount(oldAgentId);
        }

        // Assign to new agent
        conversation.setAssignedAgent(newAgentId);
        conversationRepository.save(conversation);

        // Increment new agent's count
        agentPresenceService.incrementChatCount(newAgentId);

        log.info("Conversation {} reassigned from {} to {}", conversationId, oldAgentId, newAgentId);
        return true;
    }

    /**
     * Unassign conversation (remove agent)
     */
    @Transactional
    public void unassignConversation(String conversationId) {
        log.info("Unassigning conversation: {}", conversationId);

        ChatConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));

        String agentId = conversation.getAssignedAgent();
        if (agentId != null) {
            agentPresenceService.decrementChatCount(agentId);
        }

        conversation.setAssignedAgent(null);
        conversation.setStatus(ConversationStatus.NEW);
        conversationRepository.save(conversation);

        log.info("Conversation {} unassigned", conversationId);
    }

    /**
     * Get queue depth (số conversation NEW chưa assign)
     */
    public long getQueueDepth() {
        return conversationRepository.countByStatus(ConversationStatus.NEW);
    }

    /**
     * Get unassigned conversations
     */
    public List<ChatConversationEntity> getUnassignedConversations() {
        return conversationRepository.findByAssignedAgentIsNullAndStatus(ConversationStatus.NEW);
    }

    /**
     * Batch auto assign tất cả conversations đang NEW
     */
    @Transactional
    public int batchAutoAssign() {
        log.info("Starting batch auto assignment");

        List<ChatConversationEntity> waitingConversations = getUnassignedConversations();
        int assignedCount = 0;

        for (ChatConversationEntity conversation : waitingConversations) {
            try {
                boolean assigned = autoAssignConversation(conversation.getConversationId());
                if (assigned) {
                    assignedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to auto assign conversation {}: {}", conversation.getConversationId(), e.getMessage());
            }
        }

        log.info("Batch auto assignment completed: {} out of {} assigned", assignedCount, waitingConversations.size());
        return assignedCount;
    }
}
