package com.martinsdev.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.martinsdev.auth.infra.security.RsaKeyProvider;
import com.martinsdev.auth.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RsaKeyProvider keyProvider;

    public String geracaoToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.RSA256(keyProvider.rsaPublicKey(), keyProvider.rsaPrivateKey());
            return JWT.create()
                    .withIssuer("auth-service")
                    .withSubject(usuario.getEmail()) // A quem se refere o token vai ser identificado pelo email(unique)
                    .withClaim("user_id", String.valueOf(usuario.getId()))
                    .withClaim("name", usuario.getNome())
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(expirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw  new RuntimeException("Error generation JWT token", exception);
        }
    }

    private Instant expirationDate() {
        return Instant.now().plus(40, ChronoUnit.MINUTES);
    }
}
