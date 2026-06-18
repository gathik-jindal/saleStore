package com.ticketstop.ingestion.client;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ticketstop.ingestion.config.IngestionProperties;

@Component
public class IdentityClient {
    private final RestClient restClient;

    public IdentityClient(RestClient.Builder builder, IngestionProperties properties) {
        this.restClient = builder.baseUrl(properties.identityBaseUrl()).build();
    }

    public AuthenticatedUser authenticate(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header must contain a Bearer token");
        }
        UserResponse response = restClient.get()
            .uri("/api/v1/auth/me")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            .retrieve()
            .body(UserResponse.class);
        if (response == null) {
            throw new IllegalArgumentException("Identity service returned no user");
        }
        return new AuthenticatedUser(response.id().toString(), response.email());
    }

    public record AuthenticatedUser(String id, String email) {}
    private record UserResponse(UUID id, String email, String fullName, String createdAt) {}
}
