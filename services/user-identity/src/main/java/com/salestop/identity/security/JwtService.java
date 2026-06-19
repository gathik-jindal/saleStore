package com.salestop.identity.security;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.salestop.identity.config.IdentityProperties;
import com.salestop.identity.domain.UserAccount;

@Service
public class JwtService {
    private static final List<String> DEFAULT_ROLES = List.of("USER");

    private final IdentityProperties properties;
    private final RsaKeyProvider keyProvider;

    public JwtService(IdentityProperties properties, RsaKeyProvider keyProvider) {
        this.properties = properties;
        this.keyProvider = keyProvider;
    }

    public String issueAccessToken(UserAccount user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.jwt().accessTokenTtl());
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .issuer(properties.jwt().issuer())
            .audience(properties.jwt().audience())
            .subject(user.getId().toString())
            .issueTime(Date.from(now))
            .expirationTime(Date.from(expiresAt))
            .claim("email", user.getEmail())
            .claim("roles", DEFAULT_ROLES)
            .build();

        SignedJWT jwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyProvider.keyId()).build(),
            claims
        );
        try {
            jwt.sign(new RSASSASigner(keyProvider.privateKey()));
            return jwt.serialize();
        } catch (JOSEException ex) {
            throw new IllegalStateException("Unable to sign access token", ex);
        }
    }

    public JwtPrincipal validate(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            boolean verified = jwt.verify(new RSASSAVerifier(keyProvider.publicKey()));
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (!verified || claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
                throw new InvalidTokenException("Token is expired or invalid");
            }
            if (!properties.jwt().issuer().equals(claims.getIssuer())) {
                throw new InvalidTokenException("Token issuer is invalid");
            }
            if (!claims.getAudience().contains(properties.jwt().audience())) {
                throw new InvalidTokenException("Token audience is invalid");
            }
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.getStringClaim("email");
            List<String> roles = claims.getStringListClaim("roles");
            return new JwtPrincipal(userId, email, roles == null ? List.of() : roles);
        } catch (InvalidTokenException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidTokenException("Token could not be parsed");
        }
    }

    public OffsetDateTime refreshTokenExpiry() {
        return OffsetDateTime.now(ZoneOffset.UTC).plus(properties.jwt().refreshTokenTtl());
    }

    public String jwksJson() {
        return new JWKSet(keyProvider.jwk()).toString();
    }

    public String keyId() {
        return keyProvider.keyId();
    }

    public String publicKeyPem() {
        return keyProvider.publicKeyPem();
    }
}
