package com.healthdekho.exception;

public class DuplicatePolicyException extends RuntimeException {
    public DuplicatePolicyException() {
        super("Customer has already registered this insurance product.");
    }
}
