package com.healthdekho.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecommendationHistoryEntryResponse(
        UUID requestId,
        Instant requestDate,
        String status,
        String insuranceGoals,
        Integer preferredPolicyTermMonths,
        String insuranceType,
        Integer calculatedAge,
        List<RecommendationItemResponse> recommendations) {
}
