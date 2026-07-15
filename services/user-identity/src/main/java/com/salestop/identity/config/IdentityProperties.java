package com.salestop.identity.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity")
public record IdentityProperties(Grpc grpc, Jwt jwt) {
  public record Grpc(int port) {}

  public record Jwt(
      String issuer, String audience, Duration accessTokenTtl, Duration refreshTokenTtl) {}
}
