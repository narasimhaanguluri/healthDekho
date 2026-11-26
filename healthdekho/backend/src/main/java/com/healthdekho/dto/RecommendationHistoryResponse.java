package com.healthdekho.dto;

import java.util.List;

public record RecommendationHistoryResponse(
        List<RecommendationHistoryEntryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
