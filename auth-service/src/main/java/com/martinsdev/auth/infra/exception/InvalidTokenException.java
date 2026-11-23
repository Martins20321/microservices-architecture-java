package com.martinsdev.auth.infra.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super("Invalid or expiration JWT Token");
    }
}
