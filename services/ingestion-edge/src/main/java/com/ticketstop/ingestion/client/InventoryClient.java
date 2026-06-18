package com.ticketstop.ingestion.client;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ticketstop.ingestion.config.IngestionProperties;

@Component
public class InventoryClient {
    private final RestClient restClient;

    public InventoryClient(RestClient.Builder builder, IngestionProperties properties) {
        this.restClient = builder.baseUrl(properties.inventoryBaseUrl()).build();
    }

    public ReservationResponse reserve(UUID productId, String userId, int quantity, String idempotencyKey) {
        return restClient.post()
            .uri("/api/v1/inventory/reserve")
            .body(new ReserveInventoryRequest(productId, userId, quantity, idempotencyKey))
            .retrieve()
            .body(ReservationResponse.class);
    }

    private record ReserveInventoryRequest(UUID productId, String userId, int quantity, String idempotencyKey) {}
    public record ReservationResponse(boolean accepted, String reason, UUID reservationId, UUID productId,
                                      String userId, int quantity, OffsetDateTime expiresAt) {}
}
