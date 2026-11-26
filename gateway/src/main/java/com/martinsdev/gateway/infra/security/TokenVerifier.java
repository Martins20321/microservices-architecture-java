package com.martinsdev.gateway.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.martinsdev.gateway.infra.exception.InvalidTokenException;
import org.springframework.stereotype.Service;

@Service
public class TokenVerifier {

    private final PublicKeyProvider publicKeyProvider;

    public TokenVerifier(PublicKeyProvider publicKeyProvider) {
        this.publicKeyProvider = publicKeyProvider;
    }

    public String getSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.RSA256(publicKeyProvider.rsaPublicKey(), null);
            return JWT.require(algorithm)
                    .withIssuer("auth-service")
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTVerificationException exception) {
            throw new InvalidTokenException("Invalid or expired token JWT", exception);
        }
    }
}
