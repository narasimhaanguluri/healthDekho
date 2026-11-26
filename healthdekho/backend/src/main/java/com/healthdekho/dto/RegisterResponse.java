package com.healthdekho.dto;

import java.util.UUID;

public record RegisterResponse(UUID customerId, String message) {
}
