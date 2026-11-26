package com.healthdekho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiTestRequest(
        @NotBlank(message = "Message is required") @Size(max = 2000) String message) {
}
