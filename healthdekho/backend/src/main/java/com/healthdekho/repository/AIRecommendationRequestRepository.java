package com.healthdekho.repository;

import com.healthdekho.entity.AIRecommendationRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIRecommendationRequestRepository extends JpaRepository<AIRecommendationRequest, UUID> {
    Page<AIRecommendationRequest> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);
}
