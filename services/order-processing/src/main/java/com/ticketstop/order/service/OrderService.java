package com.ticketstop.order.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketstop.order.domain.OrderLedger;
import com.ticketstop.order.dto.OrderIntentRequest;
import com.ticketstop.order.dto.OrderResponse;
import com.ticketstop.order.repository.OrderLedgerRepository;

@Service
public class OrderService {
    private final OrderLedgerRepository orders;

    public OrderService(OrderLedgerRepository orders) {
        this.orders = orders;
    }

    @Transactional
    public OrderResponse acceptIntent(OrderIntentRequest request) {
        return orders.findByIdempotencyKey(request.idempotencyKey())
            .map(OrderResponse::from)
            .orElseGet(() -> OrderResponse.from(orders.save(new OrderLedger(
                UUID.randomUUID(),
                request.productId(),
                request.userId(),
                request.reservationId(),
                request.idempotencyKey(),
                request.quantity(),
                request.unitPrice(),
                request.discountAmount()
            ))));
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        return orders.findById(id).map(OrderResponse::from)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        return orders.findAll().stream().map(OrderResponse::from).toList();
    }
}
