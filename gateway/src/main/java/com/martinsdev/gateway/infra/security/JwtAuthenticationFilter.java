package com.martinsdev.gateway.infra.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinsdev.gateway.infra.exception.ErrorResponse;
import com.martinsdev.gateway.infra.exception.InvalidTokenException;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final ObjectMapper objectMapper;
    private final TokenVerifier tokenVerifier;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final List<String> rotasPublicas = List.of("/auth-service/v1/auth/register", "/auth-service/v1/auth/login");

    public JwtAuthenticationFilter(ObjectMapper objectMapper, TokenVerifier tokenVerifier) {
        this.objectMapper = objectMapper;
        this.tokenVerifier = tokenVerifier;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        try {

            if (!rotasPublicas.contains(String.valueOf(exchange.getRequest().getPath()))) {
                String token = recuperarToken(exchange);

                if (token == null) {
                    throw new InvalidTokenException("The JWT token wasn't sent in the request");
                }

                tokenVerifier.verify(token);
            }
            return chain.filter(exchange);
        } catch (InvalidTokenException exception) {
            try {
                return getVoidMono(exchange, exception);
            } catch (JsonProcessingException e) {
                HttpStatus status = HttpStatus.UNAUTHORIZED;
                exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(status.value()));

                log.error("Erro ao serializar o errorJson de token inválido. Exception: ", e);
                return exchange.getResponse().setComplete();
            }
        }

    }

    @Nonnull
    private Mono<Void> getVoidMono(ServerWebExchange exchange, InvalidTokenException exception) throws JsonProcessingException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorResponse errorResponse = new ErrorResponse(Instant.now(), status.value(), "Unauthorized", exception.getMessage(), exchange.getRequest().getPath().toString());
        String errorJson = objectMapper.writeValueAsString(errorResponse);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatusCode.valueOf(status.value()));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory factory = response.bufferFactory();
        byte[] decode = errorJson.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = factory.wrap(decode);

        return response.writeWith(Mono.just(buffer));
    }

    private String recuperarToken(ServerWebExchange exchange) {
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.replace("Bearer ", "").strip();
        }

        return null;
    }
}
