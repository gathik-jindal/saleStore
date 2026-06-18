package com.ticketstop.catalog.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ticketstop.catalog.domain.Product;

public record ProductResponse(
    UUID id,
    String sku,
    String title,
    String description,
    BigDecimal price,
    int initialStock,
    boolean active,
    OffsetDateTime createdAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getSku(),
            product.getTitle(),
            product.getDescription(),
            product.getPrice(),
            product.getInitialStock(),
            product.isActive(),
            product.getCreatedAt()
        );
    }
}
