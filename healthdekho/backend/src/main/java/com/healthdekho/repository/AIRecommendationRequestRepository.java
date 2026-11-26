package com.healthdekho.repository;

import com.healthdekho.entity.AIRecommendationRequest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIRecommendationRequestRepository extends JpaRepository<AIRecommendationRequest, UUID> {
}
