package com.salestop.edge;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.SignedJWT;
import java.net.URI;
import java.net.http.*;
import java.security.interfaces.RSAPublicKey;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class JwtVerifier {
  private final URI jwksUri;
  private final String issuer;
  private final String audience;
  private final Duration ttl;
  private final HttpClient http =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
  private final Map<String, RSAPublicKey> keys = new ConcurrentHashMap<>();
  private volatile Instant refreshAfter = Instant.EPOCH;

  JwtVerifier(
      @Value("${edge.jwt.jwks-uri:http://localhost:8081/.well-known/jwks.json}") URI jwksUri,
      @Value("${edge.jwt.issuer:salestop-user-identity}") String issuer,
      @Value("${edge.jwt.audience:salestop}") String audience,
      @Value("${edge.jwt.cache-ttl:PT5M}") Duration ttl) {
    this.jwksUri = jwksUri;
    this.issuer = issuer;
    this.audience = audience;
    this.ttl = ttl;
  }

  UUID verify(String token) {
    try {
      SignedJWT jwt = SignedJWT.parse(token);
      if (!JWSAlgorithm.RS256.equals(jwt.getHeader().getAlgorithm()))
        throw new Unauthorized("Unsupported token algorithm");
      String kid = jwt.getHeader().getKeyID();
      if (kid == null) throw new Unauthorized("Token has no key id");
      if (Instant.now().isAfter(refreshAfter) || !keys.containsKey(kid)) refresh();
      RSAPublicKey key = keys.get(kid);
      if (key == null || !jwt.verify(new RSASSAVerifier(key)))
        throw new Unauthorized("Invalid token signature");
      var claims = jwt.getJWTClaimsSet();
      Date expiration = claims.getExpirationTime();
      if (expiration == null || expiration.toInstant().isBefore(Instant.now()))
        throw new Unauthorized("Token expired");
      if (!issuer.equals(claims.getIssuer()) || !claims.getAudience().contains(audience))
        throw new Unauthorized("Invalid token claims");
      return UUID.fromString(claims.getSubject());
    } catch (Unauthorized ex) {
      throw ex;
    } catch (Exception ex) {
      throw new Unauthorized("Invalid access token");
    }
  }

  private synchronized void refresh() throws Exception {
    if (Instant.now().isBefore(refreshAfter) && !keys.isEmpty()) return;
    HttpRequest request =
        HttpRequest.newBuilder(jwksUri).timeout(Duration.ofSeconds(3)).GET().build();
    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() / 100 != 2)
      throw new IllegalStateException("JWKS endpoint returned " + response.statusCode());
    JWKSet set = JWKSet.parse(response.body());
    Map<String, RSAPublicKey> refreshed = new HashMap<>();
    for (JWK jwk : set.getKeys())
      if (jwk instanceof RSAKey rsa && rsa.getKeyID() != null)
        refreshed.put(rsa.getKeyID(), rsa.toRSAPublicKey());
    if (refreshed.isEmpty()) throw new IllegalStateException("JWKS contains no RSA keys");
    keys.clear();
    keys.putAll(refreshed);
    refreshAfter = Instant.now().plus(ttl);
  }

  static class Unauthorized extends RuntimeException {
    Unauthorized(String message) {
      super(message);
    }
  }
}
