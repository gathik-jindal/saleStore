package com.salestop.orders;

import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
  boolean existsByIdempotencyKey(String key);

  List<OrderEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}

interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
  List<OutboxEvent> findByPublishedAtIsNullOrderByCreatedAtAsc(Pageable page);
}
