package com.healthdekho.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "insurance_product")
@Getter
@Setter
public class InsuranceProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "product_type", nullable = false, length = 50)
    private String productType;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "sum_insured", nullable = false, precision = 19, scale = 2)
    private BigDecimal sumInsured;

    @Column(name = "premium_per_installment", nullable = false, precision = 19, scale = 2)
    private BigDecimal premiumPerInstallment;

    @Column(name = "premium_frequency", nullable = false, length = 30)
    private String premiumFrequency;

    @Column(name = "min_age", nullable = false)
    private Integer minAge;

    @Column(name = "max_age", nullable = false)
    private Integer maxAge;

    @Column(nullable = false)
    private boolean active = true;
}
