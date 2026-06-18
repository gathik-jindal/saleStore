package com.ticketstop.inventory.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.ticketstop.inventory.domain.InventoryItem;

import jakarta.persistence.LockModeType;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryItem> findLockedByProductId(UUID productId);
}
