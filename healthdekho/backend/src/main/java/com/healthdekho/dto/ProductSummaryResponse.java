package com.healthdekho.dto;

public record ProductSummaryResponse(
        String productCode,
        String policyName,
        String policyType,
        Integer termMonths,
        String premiumFrequency,
        String premiumAmount,
        String sumInsured,
        Integer minAge,
        Integer maxAge) {
}
