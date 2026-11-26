package com.martinsdev.gateway.infra.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message, JWTVerificationException exception) {
        super(message);
    }
}
