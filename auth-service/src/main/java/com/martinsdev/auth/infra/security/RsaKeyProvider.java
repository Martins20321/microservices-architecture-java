package com.martinsdev.auth.infra.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RsaKeyProvider {

    @Value("${jwt.private-key}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key}")
    private Resource publicKeyResource;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @PostConstruct
    void load() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException { // a leitura acontece so uma vez
        this.privateKey = loadPrivateKey();
        this.publicKey = loadPublicKey();
    }

    public RSAPrivateKey rsaPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey rsaPublicKey() {
        return publicKey;
    }

    private RSAPrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (InputStream inputStream = privateKeyResource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8); // ler o arquito, bytes -> Texto
            String contentKey = content.strip()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "");

            byte[] decode = Base64.getMimeDecoder().decode(contentKey);
            PKCS8EncodedKeySpec privateKeySpecification = new PKCS8EncodedKeySpec(decode); // especificao para passar para o KeyFactory formar
            KeyFactory keyFactoryRSA = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactoryRSA.generatePrivate(privateKeySpecification);
        }

    }

    private RSAPublicKey loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (InputStream inputStream = publicKeyResource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8); // ler o arquito, bytes -> Texto
            String contentKey = content.strip()
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "");

            byte[] decode = Base64.getMimeDecoder().decode(contentKey);
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(decode);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(x509EncodedKeySpec);
        }
    }
}
