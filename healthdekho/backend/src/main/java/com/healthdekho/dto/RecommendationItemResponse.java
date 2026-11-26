package com.healthdekho.dto;

import java.math.BigDecimal;

public record RecommendationItemResponse(
        String productCode,
        String policyName,
        String policyType,
        Integer policyTermMonths,
        BigDecimal premiumAmount,
        BigDecimal sumInsured,
        String recommendationReason,
        String suitability,
        Integer rank) {
}
