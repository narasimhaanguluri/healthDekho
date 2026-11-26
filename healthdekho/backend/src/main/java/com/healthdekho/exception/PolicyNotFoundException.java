package com.healthdekho.exception;

public class PolicyNotFoundException extends RuntimeException {
    public PolicyNotFoundException() {
        super("The requested policy was not found.");
    }
}
