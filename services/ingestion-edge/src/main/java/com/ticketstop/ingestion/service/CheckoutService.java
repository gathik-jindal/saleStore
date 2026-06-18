package com.ticketstop.ingestion.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.ticketstop.ingestion.client.IdentityClient;
import com.ticketstop.ingestion.client.InventoryClient;
import com.ticketstop.ingestion.client.OrderClient;
import com.ticketstop.ingestion.client.PromoClient;
import com.ticketstop.ingestion.dto.CheckoutRequest;
import com.ticketstop.ingestion.dto.CheckoutResponse;

@Service
public class CheckoutService {
    private final IdentityClient identityClient;
    private final PromoClient promoClient;
    private final InventoryClient inventoryClient;
    private final OrderClient orderClient;

    public CheckoutService(IdentityClient identityClient, PromoClient promoClient,
                           InventoryClient inventoryClient, OrderClient orderClient) {
        this.identityClient = identityClient;
        this.promoClient = promoClient;
        this.inventoryClient = inventoryClient;
        this.orderClient = orderClient;
    }

    public CheckoutResponse checkout(String authorizationHeader, CheckoutRequest request) {
        IdentityClient.AuthenticatedUser user = identityClient.authenticate(authorizationHeader);
        BigDecimal orderAmount = request.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));
        PromoClient.PromoResult promo = promoClient.validate(request.promoCode(), orderAmount);
        if (!promo.valid()) {
            return new CheckoutResponse(false, promo.reason(), null, null, BigDecimal.ZERO, orderAmount);
        }
        InventoryClient.ReservationResponse reservation = inventoryClient.reserve(
            request.productId(),
            user.id(),
            request.quantity(),
            request.idempotencyKey()
        );
        if (reservation == null || !reservation.accepted()) {
            return new CheckoutResponse(false, reservation == null ? "Inventory service returned no response" : reservation.reason(),
                null, null, promo.discountAmount(), promo.finalAmount());
        }
        OrderClient.OrderResponse order = orderClient.publishIntent(request.productId(), user.id(), reservation.reservationId(),
            request.idempotencyKey(), request.quantity(), request.unitPrice(), promo.discountAmount());
        return new CheckoutResponse(true, "accepted", reservation.reservationId(), order == null ? null : order.id(),
            promo.discountAmount(), order == null ? promo.finalAmount() : order.totalAmount());
    }
}
