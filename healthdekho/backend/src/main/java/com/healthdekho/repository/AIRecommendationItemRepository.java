package com.healthdekho.repository;

import com.healthdekho.entity.AIRecommendationItem;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AIRecommendationItemRepository extends JpaRepository<AIRecommendationItem, UUID> {
    @Query("""
            SELECT i FROM AIRecommendationItem i
            JOIN FETCH i.product p
            WHERE i.request.id IN :requestIds
            ORDER BY i.request.id ASC, i.rank ASC
            """)
    List<AIRecommendationItem> findWithProductByRequestIdInOrderByRankAsc(
            @Param("requestIds") Collection<UUID> requestIds);

    @Query("""
            SELECT i FROM AIRecommendationItem i
            JOIN FETCH i.product p
            WHERE i.request.id = :requestId
            ORDER BY i.rank ASC
            """)
    List<AIRecommendationItem> findByRequestIdOrderByRankAsc(@Param("requestId") UUID requestId);
}
