package com.healthdekho.exception;

public class AiRecommendationUnavailableException extends RuntimeException {
    public AiRecommendationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public AiRecommendationUnavailableException(String message) {
        super(message);
    }
}
