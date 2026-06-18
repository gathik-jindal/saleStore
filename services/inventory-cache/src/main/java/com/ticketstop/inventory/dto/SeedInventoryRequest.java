package com.ticketstop.inventory.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SeedInventoryRequest(@NotNull UUID productId, @Min(0) int stock) {}
