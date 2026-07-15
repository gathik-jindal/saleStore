package com.salestop.common.events;

import java.time.Instant;
import java.util.UUID;

public record PaymentStatusEvent(
    UUID orderId, UUID userId, String status, String providerReference, Instant occurredAt) {}
