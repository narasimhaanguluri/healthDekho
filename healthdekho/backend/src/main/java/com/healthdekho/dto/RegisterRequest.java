package com.healthdekho.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 7, max = 30) @Pattern(
                regexp = "^[+]?[0-9][0-9\\s-]{6,29}$",
                message = "Mobile number must contain digits and optional +, spaces, or hyphens")
        String mobileNumber,
        @NotNull @PastOrPresent LocalDate dateOfBirth,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(min = 8, max = 72) String confirmPassword) {

    @AssertTrue(message = "Password and confirm password must match")
    public boolean isPasswordsMatching() {
        return password == null || confirmPassword == null || password.equals(confirmPassword);
    }

    @AssertTrue(message = "Password exceeds the supported BCrypt byte length")
    public boolean isPasswordBcryptCompatible() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
