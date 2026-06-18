package com.ticketstop.inventory.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReserveInventoryRequest(
    @NotNull UUID productId,
    @NotBlank String userId,
    @Min(1) int quantity,
    @NotBlank String idempotencyKey
) {}
