package com.example.chatcore.controller;

import com.example.chatcore.dto.AgentDTO.*;
import com.example.chatcore.enums.AgentStatus;
import com.example.chatcore.service.AgentPresenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Agent Management
 * APIs quản lý agent status, capacity, heartbeat
 */
@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
@Slf4j
public class AgentController {

    private final AgentPresenceService agentPresenceService;

    /**
     * POST /api/v1/agents - Tạo agent mới
     */
    @PostMapping
    public ResponseEntity<AgentResponse> createAgent(@Valid @RequestBody CreateAgentRequest request) {
        log.info("Creating agent: {}", request.name());
        AgentResponse response = agentPresenceService.createAgent(request);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/agents/{agentId}/status - Update agent status
     */
    @PutMapping("/{agentId}/status")
    public ResponseEntity<AgentResponse> updateAgentStatus(
            @PathVariable String agentId,
            @Valid @RequestBody UpdateAgentStatusRequest request) {
        log.info("Updating agent {} status to {}", agentId, request.status());
        AgentResponse response = agentPresenceService.updateAgentStatus(agentId, request.status());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/agents/{agentId}/heartbeat - Agent heartbeat
     */
    @PostMapping("/{agentId}/heartbeat")
    public ResponseEntity<Void> trackHeartbeat(@PathVariable String agentId) {
        agentPresenceService.trackHeartbeat(agentId);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/v1/agents/{agentId}/capacity - Update agent capacity
     */
    @PutMapping("/{agentId}/capacity")
    public ResponseEntity<AgentResponse> updateAgentCapacity(
            @PathVariable String agentId,
            @Valid @RequestBody UpdateAgentCapacityRequest request) {
        log.info("Updating agent {} capacity to {}", agentId, request.maxConcurrentChats());
        AgentResponse response = agentPresenceService.updateAgentCapacity(agentId, request.maxConcurrentChats());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/agents/{agentId} - Get agent by ID
     */
    @GetMapping("/{agentId}")
    public ResponseEntity<AgentResponse> getAgentById(@PathVariable String agentId) {
        AgentResponse response = agentPresenceService.getAgentById(agentId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/agents/online - Get online agents
     */
    @GetMapping("/online")
    public ResponseEntity<AvailableAgentsResponse> getOnlineAgents() {
        List<AgentResponse> agents = agentPresenceService.getOnlineAgents();
        return ResponseEntity.ok(new AvailableAgentsResponse(agents, agents.size()));
    }

    /**
     * GET /api/v1/agents/available - Get available agents (ONLINE + có capacity)
     */
    @GetMapping("/available")
    public ResponseEntity<AvailableAgentsResponse> getAvailableAgents() {
        List<AgentResponse> agents = agentPresenceService.getAvailableAgents();
        return ResponseEntity.ok(new AvailableAgentsResponse(agents, agents.size()));
    }

    /**
     * GET /api/v1/agents/skills/{skills} - Get agents by skills
     */
    @GetMapping("/skills/{skills}")
    public ResponseEntity<AvailableAgentsResponse> getAgentsBySkills(@PathVariable String skills) {
        List<AgentResponse> agents = agentPresenceService.getAgentsBySkills(skills);
        return ResponseEntity.ok(new AvailableAgentsResponse(agents, agents.size()));
    }

    /**
     * POST /api/v1/agents/mark-stale-offline - Mark stale agents as offline
     */
    @PostMapping("/mark-stale-offline")
    public ResponseEntity<Void> markStaleAgentsAsOffline() {
        log.info("Marking stale agents as offline");
        agentPresenceService.markStaleAgentsAsOffline();
        return ResponseEntity.ok().build();
    }
}
