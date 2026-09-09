package com.example.chatcore.controller;

import com.example.chatcore.dto.RatingDTO.*;
import com.example.chatcore.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST Controller cho Rating/CSAT Management
 * APIs đánh giá conversation và tính CSAT
 */
@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
@Slf4j
public class RatingController {

    private final RatingService ratingService;

    /**
     * POST /api/v1/ratings/conversations/{conversationId} - Submit rating
     */
    @PostMapping("/conversations/{conversationId}")
    public ResponseEntity<RatingResponse> submitRating(
            @PathVariable String conversationId,
            @Valid @RequestBody SubmitRatingRequest request) {
        log.info("Submitting rating for conversation {}: {}/5", conversationId, request.rating());
        RatingResponse response = ratingService.submitRating(conversationId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/ratings/conversations/{conversationId} - Get rating by conversation
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<RatingResponse> getRatingByConversation(@PathVariable String conversationId) {
        RatingResponse response = ratingService.getRatingByConversation(conversationId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/ratings/csat/score - Get overall CSAT score
     */
    @GetMapping("/csat/score")
    public ResponseEntity<CsatScoreResponse> getCsatScore() {
        CsatScoreResponse response = ratingService.calculateCsatScore();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/ratings/csat/report - Get CSAT report trong khoảng thời gian
     */
    @GetMapping("/csat/report")
    public ResponseEntity<CsatReportResponse> getCsatReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        CsatReportResponse response = ratingService.calculateCsatReport(startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
