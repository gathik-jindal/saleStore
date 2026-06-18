package com.ticketstop.inventory.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservationResponse(boolean accepted, String reason, UUID reservationId, UUID productId,
                                  String userId, int quantity, OffsetDateTime expiresAt) {}
