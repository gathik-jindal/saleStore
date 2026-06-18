package com.ticketstop.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ingestion")
public record IngestionProperties(
    String identityBaseUrl,
    String inventoryBaseUrl,
    String promoBaseUrl,
    String orderBaseUrl
) {}
