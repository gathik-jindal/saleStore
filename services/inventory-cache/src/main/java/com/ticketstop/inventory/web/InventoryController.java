package com.ticketstop.inventory.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ticketstop.inventory.dto.InventoryResponse;
import com.ticketstop.inventory.dto.ReservationResponse;
import com.ticketstop.inventory.dto.ReserveInventoryRequest;
import com.ticketstop.inventory.dto.SeedInventoryRequest;
import com.ticketstop.inventory.service.InventoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/seed")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse seed(@Valid @RequestBody SeedInventoryRequest request) {
        return inventoryService.seed(request);
    }

    @GetMapping("/{productId}")
    public InventoryResponse availability(@PathVariable UUID productId) {
        return inventoryService.availability(productId);
    }

    @PostMapping("/reserve")
    public ReservationResponse reserve(@Valid @RequestBody ReserveInventoryRequest request) {
        return inventoryService.reserve(request);
    }
}
