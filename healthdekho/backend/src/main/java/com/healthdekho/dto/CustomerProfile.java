package com.healthdekho.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CustomerProfile(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String mobileNumber,
        LocalDate dateOfBirth) {
}
