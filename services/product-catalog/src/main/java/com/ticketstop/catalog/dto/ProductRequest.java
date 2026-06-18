package com.ticketstop.catalog.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
    @NotBlank @Size(max = 80) String sku,
    @NotBlank @Size(max = 255) String title,
    @Size(max = 2000) String description,
    @NotNull @DecimalMin("0.00") BigDecimal price,
    @Min(0) int initialStock,
    Boolean active
) {}
