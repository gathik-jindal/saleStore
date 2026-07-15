package com.salestop.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/orders")
class OrderController {
  private final OrderRepository orders;

  OrderController(OrderRepository orders) {
    this.orders = orders;
  }

  @GetMapping("/{id}")
  OrderResponse get(@PathVariable UUID id) {
    return OrderResponse.from(
        orders
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")));
  }

  @GetMapping
  List<OrderResponse> byUser(@RequestParam UUID userId) {
    return orders.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(OrderResponse::from)
        .toList();
  }

  record OrderResponse(
      UUID id,
      long productId,
      UUID userId,
      int quantity,
      String status,
      BigDecimal total,
      Instant createdAt) {
    static OrderResponse from(OrderEntity o) {
      return new OrderResponse(
          o.id(), o.productId(), o.userId(), o.quantity(), o.status(), o.total(), o.createdAt());
    }
  }
}
