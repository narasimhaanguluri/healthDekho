package com.healthdekho.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyRegistrationResponse(
        String policyNumber,
        String productCode,
        String policyName,
        LocalDate startDate,
        LocalDate maturityDate,
        BigDecimal totalPremium,
        int installmentCount,
        String message) {
}
