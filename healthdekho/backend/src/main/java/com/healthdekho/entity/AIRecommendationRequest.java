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
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ai_recommendation_request")
@Getter
@Setter
public class AIRecommendationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(length = 2000)
    private String goals;

    @Column(name = "preferred_term_months")
    private Integer preferredTermMonths;

    @Column(name = "preferred_product_type", length = 50)
    private String preferredProductType;

    @Column(name = "calculated_age")
    private Integer calculatedAge;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "model_id", length = 200)
    private String modelId;

    @Column(name = "prompt_version", length = 100)
    private String promptVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "candidate_snapshot")
    private String candidateSnapshot;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private Instant createdAt;
}
