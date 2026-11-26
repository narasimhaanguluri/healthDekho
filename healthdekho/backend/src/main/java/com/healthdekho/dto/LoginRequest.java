package com.healthdekho.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;

public record LoginRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 128) String password) {

    @AssertTrue(message = "Password exceeds the supported BCrypt byte length")
    public boolean isPasswordBcryptCompatible() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
