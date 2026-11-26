package com.healthdekho.dto;

import java.time.LocalDate;

public record PolicySummaryResponse(
        String policyNumber,
        String policyName,
        String policyType,
        Integer policyTerm,
        LocalDate startDate,
        LocalDate maturityDate,
        String premiumFrequency,
        String premiumAmount,
        String sumInsured,
        String policyStatus) {
}
