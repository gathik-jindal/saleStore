package com.salestop.identity.security;

import com.nimbusds.jose.jwk.RSAKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RsaKeyProvider {
  private final String keyId;
  private final KeyPair keyPair;

  public RsaKeyProvider(
      @Value("${identity.jwt.private-key-path:}") String privateKeyPath,
      @Value("${identity.jwt.public-key-path:}") String publicKeyPath,
      @Value("${identity.jwt.key-id:}") String configuredKeyId) {
    try {
      if (!privateKeyPath.isBlank() || !publicKeyPath.isBlank()) {
        if (privateKeyPath.isBlank() || publicKeyPath.isBlank()) {
          throw new IllegalArgumentException("Both JWT private and public key paths are required");
        }
        KeyFactory factory = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey =
            (RSAPrivateKey)
                factory.generatePrivate(
                    new PKCS8EncodedKeySpec(readPem(privateKeyPath, "PRIVATE KEY")));
        RSAPublicKey publicKey =
            (RSAPublicKey)
                factory.generatePublic(
                    new X509EncodedKeySpec(readPem(publicKeyPath, "PUBLIC KEY")));
        this.keyPair = new KeyPair(publicKey, privateKey);
      } else {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        this.keyPair = generator.generateKeyPair();
      }
      this.keyId =
          configuredKeyId.isBlank()
              ? fingerprint((RSAPublicKey) keyPair.getPublic())
              : configuredKeyId;
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to create JWT signing key", ex);
    }
  }

  private byte[] readPem(String path, String type) throws Exception {
    String pem = Files.readString(Path.of(path));
    String encoded =
        pem.replace("-----BEGIN " + type + "-----", "")
            .replace("-----END " + type + "-----", "")
            .replaceAll("\\s", "");
    return Base64.getDecoder().decode(encoded);
  }

  private String fingerprint(RSAPublicKey key) throws Exception {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getEncoded());
    return Base64.getUrlEncoder().withoutPadding().encodeToString(digest).substring(0, 22);
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
    return new RSAKey.Builder(publicKey()).keyID(keyId).build();
  }

  public String publicKeyPem() {
    String encoded =
        Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(publicKey().getEncoded());
    return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----\n";
  }
}
