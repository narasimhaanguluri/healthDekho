package com.healthdekho.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
