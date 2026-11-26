package com.healthdekho.exception;

public class CustomerRegistrationConflictException extends RuntimeException {
    public CustomerRegistrationConflictException() {
        super("Customer registration could not be completed.");
    }
}
