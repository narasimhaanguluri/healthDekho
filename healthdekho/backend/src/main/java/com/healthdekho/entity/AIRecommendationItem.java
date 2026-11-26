package com.healthdekho.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ai_recommendation_item", uniqueConstraints = @UniqueConstraint(
        name = "uq_recommendation_request_product", columnNames = {"request_id", "product_id"}))
@Getter
@Setter
public class AIRecommendationItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private AIRecommendationRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private InsuranceProduct product;

    @Column(nullable = false, length = 2000)
    private String reason;

    @Column(length = 100)
    private String suitability;

    @Column(nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private boolean selected;
}
