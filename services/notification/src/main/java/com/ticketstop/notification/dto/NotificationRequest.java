package com.ticketstop.notification.dto;

import java.util.UUID;

import com.ticketstop.notification.domain.NotificationChannel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
    @NotBlank String userId,
    UUID orderId,
    @NotNull NotificationChannel channel,
    @NotBlank @Size(max = 320) String recipient,
    @Size(max = 255) String subject,
    @NotBlank @Size(max = 2000) String body
) {}
