package com.ticketstop.inventory.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketstop.inventory.domain.ReservationRecord;

public interface ReservationRecordRepository extends JpaRepository<ReservationRecord, UUID> {
    Optional<ReservationRecord> findByIdempotencyKey(String idempotencyKey);
    boolean existsByProductIdAndUserIdAndStatus(UUID productId, String userId, com.ticketstop.inventory.domain.ReservationStatus status);
}
