package com.example.realtime.controller;

import com.example.realtime.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Presence REST Controller
 * Provides REST API for querying user presence status
 */
@Slf4j
@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    /**
     * Get user presence status
     * GET /api/presence/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getPresence(@PathVariable String userId) {
        PresenceService.PresenceStatus status = presenceService.getPresence(userId);
        LocalDateTime lastSeen = presenceService.getLastSeen(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("status", status.name());
        response.put("isOnline", presenceService.isOnline(userId));
        response.put("lastSeen", lastSeen);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get presence for multiple users
     * POST /api/presence/bulk
     * Body: { "userIds": ["user1", "user2", "user3"] }
     */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> getBulkPresence(@RequestBody Map<String, List<String>> request) {
        List<String> userIds = request.get("userIds");
        
        if (userIds == null || userIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "userIds is required"));
        }
        
        Map<String, PresenceService.PresenceStatus> presenceMap = presenceService.getBulkPresence(userIds);
        
        Map<String, Object> response = new HashMap<>();
        presenceMap.forEach((userId, status) -> {
            Map<String, Object> userPresence = new HashMap<>();
            userPresence.put("status", status.name());
            userPresence.put("isOnline", status == PresenceService.PresenceStatus.ONLINE 
                                      || status == PresenceService.PresenceStatus.AWAY);
            response.put(userId, userPresence);
        });
        
        return ResponseEntity.ok(response);
    }

    /**
     * Manually set user status (for testing or manual control)
     * PUT /api/presence/{userId}/status
     * Body: { "status": "ONLINE" | "AWAY" | "OFFLINE" }
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<Map<String, Object>> updatePresence(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        
        String statusStr = request.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "status is required"));
        }
        
        try {
            PresenceService.PresenceStatus status = PresenceService.PresenceStatus.valueOf(statusStr.toUpperCase());
            presenceService.updatePresence(userId, status);
            
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "status", status.name(),
                    "message", "Presence updated successfully"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid status. Must be ONLINE, AWAY, or OFFLINE"
            ));
        }
    }
}
