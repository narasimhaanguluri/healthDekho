package com.healthdekho.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecommendationRequest(
        @Size(max = 2000) String insuranceGoals,
        @Positive(message = "Preferred policy term must be positive") Integer preferredPolicyTermMonths,
        @Size(max = 50) String insuranceType) {

    @AssertTrue(message = "At least one recommendation input is required")
    public boolean hasAtLeastOnePreference() {
        boolean hasGoals = insuranceGoals != null && !insuranceGoals.isBlank();
        boolean hasTerm = preferredPolicyTermMonths != null;
        boolean hasType = insuranceType != null && !insuranceType.isBlank();
        return hasGoals || hasTerm || hasType;
    }
}
