package com.healthdekho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PolicyRegistrationRequest(
        @NotBlank(message = "Product code is required") @Size(max = 50) String productCode) {
}
