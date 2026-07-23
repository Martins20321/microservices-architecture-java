package com.martinsdev.pagamentos.infra.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String id) {
        super("Resource not found by id: " + id);
    }
}
