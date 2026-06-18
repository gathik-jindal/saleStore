package com.ticketstop.inventory.dto;

import java.util.UUID;

import com.ticketstop.inventory.domain.InventoryItem;

public record InventoryResponse(UUID productId, int availableStock, int reservedStock) {
    public static InventoryResponse from(InventoryItem item) {
        return new InventoryResponse(item.getProductId(), item.getAvailableStock(), item.getReservedStock());
    }
}
