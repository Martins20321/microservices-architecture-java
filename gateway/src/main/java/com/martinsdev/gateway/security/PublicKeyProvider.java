package com.martinsdev.gateway.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class PublicKeyProvider {

    @Value("${jwt.public-key}")
    private Resource publicKeyResource;

    private RSAPublicKey rsaPublicKey;

    @PostConstruct
    void load() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        rsaPublicKey = loadPublicKey();
    }

    public RSAPublicKey rsaPublicKey() {
        return rsaPublicKey;
    }

    private RSAPublicKey loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (InputStream inputStream = publicKeyResource.getInputStream()) {
            String document = new String(inputStream.readAllBytes());
            String cleanDocument = document.strip()
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "");

            byte[] decode = Base64.getMimeDecoder().decode(cleanDocument);
            X509EncodedKeySpec specificationKey = new X509EncodedKeySpec(decode);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(specificationKey);
        }
    }
}
