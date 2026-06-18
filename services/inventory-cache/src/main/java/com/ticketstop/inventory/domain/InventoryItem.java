package com.ticketstop.inventory.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    @Column(name = "reserved_stock", nullable = false)
    private int reservedStock;

    @Version
    private long version;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected InventoryItem() {
    }

    public InventoryItem(UUID productId, int availableStock) {
        this.productId = productId;
        this.availableStock = availableStock;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = OffsetDateTime.now();
    }

    public boolean canReserve(int quantity) {
        return quantity > 0 && availableStock >= quantity;
    }

    public void reserve(int quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("Insufficient stock");
        }
        availableStock -= quantity;
        reservedStock += quantity;
    }

    public void seed(int stock) {
        availableStock = stock;
        reservedStock = 0;
    }

    public UUID getProductId() { return productId; }
    public int getAvailableStock() { return availableStock; }
    public int getReservedStock() { return reservedStock; }
}
