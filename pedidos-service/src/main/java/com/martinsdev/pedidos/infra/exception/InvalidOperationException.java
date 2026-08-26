package com.martinsdev.pedidos.infra.exception;

public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException() {
        super("This operation is not valid!");
    }
}
