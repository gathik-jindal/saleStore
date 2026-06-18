package com.ticketstop.identity.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.RSAKey;

@Component
public class RsaKeyProvider {
    private final String keyId = UUID.randomUUID().toString();
    private final KeyPair keyPair;

    public RsaKeyProvider() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            this.keyPair = generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create JWT signing key", ex);
        }
    }

    public String keyId() {
        return keyId;
    }

    public RSAPrivateKey privateKey() {
        return (RSAPrivateKey) keyPair.getPrivate();
    }

    public RSAPublicKey publicKey() {
        return (RSAPublicKey) keyPair.getPublic();
    }

    public RSAKey jwk() {
        return new RSAKey.Builder(publicKey())
            .keyID(keyId)
            .build();
    }

    public String publicKeyPem() {
        String encoded = Base64.getMimeEncoder(64, "\n".getBytes())
            .encodeToString(publicKey().getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----\n";
    }
}
