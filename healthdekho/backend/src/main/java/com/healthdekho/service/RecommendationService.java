package com.healthdekho.service;

import com.healthdekho.dto.RecommendationHistoryResponse;
import com.healthdekho.dto.RecommendationRequest;
import com.healthdekho.dto.RecommendationResponse;
import java.util.UUID;

public interface RecommendationService {
    RecommendationResponse generateRecommendations(UUID customerId, RecommendationRequest request);

    RecommendationHistoryResponse getHistory(UUID customerId, int page, int size);
}
