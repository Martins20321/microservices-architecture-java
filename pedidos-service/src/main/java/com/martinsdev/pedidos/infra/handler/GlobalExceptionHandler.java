package com.martinsdev.pedidos.infra.handler;

import com.martinsdev.pedidos.infra.exception.ErrorResponse;
import com.martinsdev.pedidos.infra.exception.InvalidOperationException;
import com.martinsdev.pedidos.infra.exception.ResourceNotFoundException;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ObjectMapper mapper = new ObjectMapper();

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        String error = "Resource not found";
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse errorResponse = new ErrorResponse(Instant.now(), status.value(), error, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(InvalidOperationException ex, HttpServletRequest request) {
        String error = "Invalid operation";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse errorResponse = new ErrorResponse(Instant.now(), status.value(), error, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(FeignException.BadRequest.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException.BadRequest ex, HttpServletRequest request){
        Map<String, Object> corpo = mapper.readValue(ex.contentUTF8(), Map.class);
        String error = corpo.get("error").toString();
        HttpStatus status = HttpStatus.valueOf(ex.status());
        ErrorResponse errorResponse = new ErrorResponse(Instant.now(), status.value(), error, corpo.get("message").toString(), request.getRequestURI());
        return ResponseEntity.status(status).body(errorResponse);
    }
}
