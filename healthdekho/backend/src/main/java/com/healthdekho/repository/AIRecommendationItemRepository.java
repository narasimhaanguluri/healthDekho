package com.healthdekho.repository;

import com.healthdekho.entity.AIRecommendationItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIRecommendationItemRepository extends JpaRepository<AIRecommendationItem, UUID> {
}
