package com.healthdekho.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RecommendationRequest(
        @PastOrPresent(message = "Date of birth cannot be in the future") LocalDate dateOfBirth,
        @Size(max = 2000) String insuranceGoals,
        @Positive(message = "Preferred policy term must be positive") Integer preferredPolicyTermMonths,
        @Size(max = 50) String insuranceType) {

    @AssertTrue(message = "At least one recommendation input is required")
    public boolean hasAtLeastOnePreference() {
        boolean hasDob = dateOfBirth != null;
        boolean hasGoals = insuranceGoals != null && !insuranceGoals.isBlank();
        boolean hasTerm = preferredPolicyTermMonths != null;
        boolean hasType = insuranceType != null && !insuranceType.isBlank();
        return hasDob || hasGoals || hasTerm || hasType;
    }
}
