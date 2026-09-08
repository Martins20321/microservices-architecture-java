package com.martinsdev.inventario.infra.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(Long id) {
        super("Resource not found by id: " + id);
    }
}
