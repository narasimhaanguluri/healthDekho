package com.healthdekho.exception;

public class ProductUnavailableException extends RuntimeException {
    public ProductUnavailableException() {
        super("The selected insurance product is no longer active.");
    }
}
