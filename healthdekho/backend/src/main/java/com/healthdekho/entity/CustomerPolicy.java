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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "customer_policy", uniqueConstraints = {
        @UniqueConstraint(name = "uq_customer_policy_product", columnNames = {"customer_id", "product_id"}),
        @UniqueConstraint(
                name = "uq_customer_policy_registration_idempotency",
                columnNames = {"customer_id", "registration_idempotency_key"})
})
@Getter
@Setter
public class CustomerPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private InsuranceProduct product;

    @Column(name = "policy_number", nullable = false, unique = true, length = 64)
    private String policyNumber;

    @Column(name = "registration_idempotency_key", nullable = false)
    private UUID registrationIdempotencyKey;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "product_type", nullable = false, length = 50)
    private String productType;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "premium_frequency", nullable = false, length = 30)
    private String premiumFrequency;

    @Column(name = "premium_per_installment", nullable = false, precision = 19, scale = 2)
    private BigDecimal premiumPerInstallment;

    @Column(name = "sum_insured", nullable = false, precision = 19, scale = 2)
    private BigDecimal sumInsured;

    @Column(name = "total_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPremium;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "registered_at", nullable = false)
    @CreationTimestamp
    private Instant registeredAt;
}
