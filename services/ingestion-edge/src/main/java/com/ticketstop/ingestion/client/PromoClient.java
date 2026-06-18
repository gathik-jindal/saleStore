package com.ticketstop.ingestion.client;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.ticketstop.ingestion.config.IngestionProperties;

@Component
public class PromoClient {
    private final RestClient restClient;

    public PromoClient(RestClient.Builder builder, IngestionProperties properties) {
        this.restClient = builder.baseUrl(properties.promoBaseUrl()).build();
    }

    public PromoResult validate(String code, BigDecimal orderAmount) {
        if (!StringUtils.hasText(code)) {
            return new PromoResult(true, "no promo", BigDecimal.ZERO, orderAmount);
        }
        PromoResult response = restClient.post()
            .uri("/api/v1/promos/validate")
            .body(new ValidateCouponRequest(code, orderAmount, true))
            .retrieve()
            .body(PromoResult.class);
        return response == null ? new PromoResult(false, "Promo service returned no response", BigDecimal.ZERO, orderAmount) : response;
    }

    private record ValidateCouponRequest(String code, BigDecimal orderAmount, boolean redeem) {}
    public record PromoResult(boolean valid, String reason, BigDecimal discountAmount, BigDecimal finalAmount) {}
}
