package com.healthdekho.controller;

import com.healthdekho.dto.RecommendationHistoryResponse;
import com.healthdekho.dto.RecommendationRequest;
import com.healthdekho.dto.RecommendationResponse;
import com.healthdekho.security.CustomerPrincipal;
import com.healthdekho.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public RecommendationResponse generateRecommendations(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @Valid @RequestBody RecommendationRequest request) {
        return recommendationService.generateRecommendations(principal.customerId(), request);
    }

    @GetMapping("/history")
    public RecommendationHistoryResponse getRecommendationHistory(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return recommendationService.getHistory(principal.customerId(), page, size);
    }
}
