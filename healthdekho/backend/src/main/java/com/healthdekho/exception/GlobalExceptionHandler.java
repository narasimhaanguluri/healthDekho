package com.healthdekho.exception;

import com.healthdekho.constants.ApiErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, Object>> authenticationFailed() {
        return problem(
                HttpStatus.UNAUTHORIZED,
                ApiErrorCodes.AUTHENTICATION_FAILED,
                "Authentication failed",
                "The supplied credentials are invalid.");
    }

    @ExceptionHandler(CustomerRegistrationConflictException.class)
    public ResponseEntity<Map<String, Object>> customerRegistrationConflict() {
        return problem(
                HttpStatus.CONFLICT,
                ApiErrorCodes.CUSTOMER_REGISTRATION_CONFLICT,
                "Registration conflict",
                "The account could not be created with the supplied details.");
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> productNotFound() {
        return problem(
                HttpStatus.NOT_FOUND,
                ApiErrorCodes.PRODUCT_NOT_FOUND,
                "Product not found",
                "The requested insurance product is unavailable.");
    }

    @ExceptionHandler(ProductUnavailableException.class)
    public ResponseEntity<Map<String, Object>> productUnavailable() {
        return problem(
                HttpStatus.CONFLICT,
                ApiErrorCodes.PRODUCT_UNAVAILABLE,
                "Product unavailable",
                "The selected insurance product is not currently active.");
    }

    @ExceptionHandler(AgeNotEligibleException.class)
    public ResponseEntity<Map<String, Object>> ageNotEligible(AgeNotEligibleException exception) {
        return problem(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ApiErrorCodes.AGE_NOT_ELIGIBLE,
                "Age not eligible",
                exception.getMessage());
    }

    @ExceptionHandler(DuplicatePolicyException.class)
    public ResponseEntity<Map<String, Object>> duplicatePolicy() {
        return problem(
                HttpStatus.CONFLICT,
                ApiErrorCodes.DUPLICATE_POLICY,
                "Duplicate policy",
                "You have already registered this insurance product.");
    }

    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<Map<String, Object>> policyNotFound() {
        return problem(
                HttpStatus.NOT_FOUND,
                ApiErrorCodes.POLICY_NOT_FOUND,
                "Policy not found",
                "The requested policy was not found.");
    }

    @ExceptionHandler(AiServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> aiServiceUnavailable() {
        return problem(
                HttpStatus.BAD_GATEWAY,
                ApiErrorCodes.AI_SERVICE_UNAVAILABLE,
                "AI service unavailable",
                "The AI provider could not complete the request.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> invalidBusinessArgument(IllegalArgumentException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorCodes.VALIDATION_FAILED,
                "Validation failed",
                exception.getMessage());
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<Map<String, Object>> invalidPagination(InvalidPaginationException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorCodes.INVALID_PAGINATION,
                "Invalid pagination",
                exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> typeMismatch() {
        return problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorCodes.VALIDATION_FAILED,
                "Validation failed",
                "One or more request parameters are invalid.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validationFailed(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (first, ignored) -> first,
                        LinkedHashMap::new));
        Map<String, Object> response = body(
                HttpStatus.BAD_REQUEST,
                ApiErrorCodes.VALIDATION_FAILED,
                "Validation failed",
                "One or more request fields are invalid.");
        response.put("fieldErrors", fieldErrors);
        return response(HttpStatus.BAD_REQUEST, response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> malformedRequest() {
        return problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorCodes.VALIDATION_FAILED,
                "Validation failed",
                "The request body is invalid.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> resourceNotFound() {
        return problem(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                "Resource not found",
                "The requested resource was not found.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> unexpectedFailure(HttpServletRequest request, Exception exception) {
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorCodes.INTERNAL_ERROR,
                "Internal server error",
                "The request could not be completed.");
    }

    private ResponseEntity<Map<String, Object>> problem(
            HttpStatus status, String code, String title, String detail) {
        return response(status, body(status, code, title, detail));
    }

    private Map<String, Object> body(HttpStatus status, String code, String title, String detail) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", title);
        body.put("status", status.value());
        body.put("detail", detail);
        body.put("code", code);
        body.put("traceId", UUID.randomUUID().toString());
        return body;
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, status);
    }
}
