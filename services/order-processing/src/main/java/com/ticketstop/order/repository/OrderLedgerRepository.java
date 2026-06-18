package com.ticketstop.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketstop.order.domain.OrderLedger;

public interface OrderLedgerRepository extends JpaRepository<OrderLedger, UUID> {
    Optional<OrderLedger> findByIdempotencyKey(String idempotencyKey);
}
