package com.healthdekho.constants;

public final class ApiErrorCodes {
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    public static final String UNAUTHENTICATED = "UNAUTHENTICATED";
    public static final String CUSTOMER_REGISTRATION_CONFLICT = "CUSTOMER_REGISTRATION_CONFLICT";
    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    public static final String PRODUCT_UNAVAILABLE = "PRODUCT_UNAVAILABLE";
    public static final String AGE_NOT_ELIGIBLE = "AGE_NOT_ELIGIBLE";
    public static final String DUPLICATE_POLICY = "DUPLICATE_POLICY";
    public static final String POLICY_NOT_FOUND = "POLICY_NOT_FOUND";
    public static final String INVALID_PAGINATION = "INVALID_PAGINATION";
    public static final String AI_RECOMMENDATION_UNAVAILABLE = "AI_RECOMMENDATION_UNAVAILABLE";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ApiErrorCodes() {
    }
}
