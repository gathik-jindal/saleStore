package com.ticketstop.order.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ticketstop.order.dto.OrderIntentRequest;
import com.ticketstop.order.dto.OrderResponse;
import com.ticketstop.order.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/intents")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderResponse acceptIntent(@Valid @RequestBody OrderIntentRequest request) {
        return orderService.acceptIntent(request);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return orderService.get(id);
    }

    @GetMapping
    public List<OrderResponse> list() {
        return orderService.list();
    }
}
