package com.healthdekho.dto;

import java.util.List;
import java.util.UUID;

public record RecommendationResponse(
        UUID requestId,
        String status,
        List<RecommendationItemResponse> recommendations) {
}
