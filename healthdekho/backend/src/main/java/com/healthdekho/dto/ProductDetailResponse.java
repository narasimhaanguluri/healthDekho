package com.healthdekho.dto;

public record ProductDetailResponse(
        String productCode,
        String policyName,
        String policyType,
        String premiumFrequency,
        String premiumAmount,
        String sumInsured,
        Integer termMonths,
        Integer minAge,
        Integer maxAge) {
}
