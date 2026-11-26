package com.healthdekho.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PolicyDetailsResponse(
        String policyNumber,
        String productCode,
        String policyName,
        String policyType,
        Integer policyTerm,
        LocalDate startDate,
        LocalDate maturityDate,
        String premiumFrequency,
        String premiumAmount,
        String sumInsured,
        String totalPremium,
        String policyStatus,
        Instant registeredAt,
        List<InstallmentSummaryResponse> installments) {
}
