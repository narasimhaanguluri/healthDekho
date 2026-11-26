package com.healthdekho.dto;

import java.time.LocalDate;

public record InstallmentSummaryResponse(
        Integer installmentNumber,
        LocalDate dueDate,
        LocalDate graceDate,
        String amount,
        String paidAmount,
        String status) {
}
