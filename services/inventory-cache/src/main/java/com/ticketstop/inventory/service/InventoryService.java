package com.ticketstop.inventory.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketstop.inventory.config.InventoryProperties;
import com.ticketstop.inventory.domain.InventoryItem;
import com.ticketstop.inventory.domain.ReservationRecord;
import com.ticketstop.inventory.domain.ReservationStatus;
import com.ticketstop.inventory.dto.InventoryResponse;
import com.ticketstop.inventory.dto.ReservationResponse;
import com.ticketstop.inventory.dto.ReserveInventoryRequest;
import com.ticketstop.inventory.dto.SeedInventoryRequest;
import com.ticketstop.inventory.repository.InventoryItemRepository;
import com.ticketstop.inventory.repository.ReservationRecordRepository;

@Service
@EnableConfigurationProperties(InventoryProperties.class)
public class InventoryService {
    private final InventoryItemRepository items;
    private final ReservationRecordRepository reservations;
    private final InventoryProperties properties;

    public InventoryService(InventoryItemRepository items, ReservationRecordRepository reservations, InventoryProperties properties) {
        this.items = items;
        this.reservations = reservations;
        this.properties = properties;
    }

    @Transactional
    public InventoryResponse seed(SeedInventoryRequest request) {
        InventoryItem item = items.findById(request.productId())
            .orElseGet(() -> new InventoryItem(request.productId(), request.stock()));
        item.seed(request.stock());
        return InventoryResponse.from(items.save(item));
    }

    @Transactional(readOnly = true)
    public InventoryResponse availability(UUID productId) {
        return items.findById(productId).map(InventoryResponse::from)
            .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));
    }

    @Transactional
    public ReservationResponse reserve(ReserveInventoryRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        ReservationRecord previous = reservations.findByIdempotencyKey(request.idempotencyKey()).orElse(null);
        if (previous != null && previous.isActive(now)) {
            return accepted(previous, "idempotent replay");
        }
        if (reservations.existsByProductIdAndUserIdAndStatus(request.productId(), request.userId(), ReservationStatus.RESERVED)) {
            return rejected(request, "User already has an active reservation");
        }
        InventoryItem item = items.findLockedByProductId(request.productId())
            .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));
        if (!item.canReserve(request.quantity())) {
            return rejected(request, "Insufficient stock");
        }
        item.reserve(request.quantity());
        ReservationRecord record = reservations.save(new ReservationRecord(
            UUID.randomUUID(),
            request.idempotencyKey(),
            request.productId(),
            request.userId(),
            request.quantity(),
            ReservationStatus.RESERVED,
            now.plus(properties.idempotencyTtl())
        ));
        return accepted(record, "reserved");
    }

    private ReservationResponse accepted(ReservationRecord record, String reason) {
        return new ReservationResponse(true, reason, record.getId(), record.getProductId(), record.getUserId(),
            record.getQuantity(), record.getExpiresAt());
    }

    private ReservationResponse rejected(ReserveInventoryRequest request, String reason) {
        return new ReservationResponse(false, reason, null, request.productId(), request.userId(), request.quantity(), null);
    }
}
