package com.ticketstop.ingestion.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ticketstop.ingestion.config.IngestionProperties;

@Component
public class OrderClient {
    private final RestClient restClient;

    public OrderClient(RestClient.Builder builder, IngestionProperties properties) {
        this.restClient = builder.baseUrl(properties.orderBaseUrl()).build();
    }

    public OrderResponse publishIntent(UUID productId, String userId, UUID reservationId, String idempotencyKey,
                                       int quantity, BigDecimal unitPrice, BigDecimal discountAmount) {
        return restClient.post()
            .uri("/api/v1/orders/intents")
            .body(new OrderIntentRequest(productId, userId, reservationId, idempotencyKey, quantity, unitPrice, discountAmount))
            .retrieve()
            .body(OrderResponse.class);
    }

    private record OrderIntentRequest(UUID productId, String userId, UUID reservationId, String idempotencyKey,
                                      int quantity, BigDecimal unitPrice, BigDecimal discountAmount) {}
    public record OrderResponse(UUID id, BigDecimal totalAmount) {}
}
