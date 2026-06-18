package com.ticketstop.notification.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.ticketstop.notification.domain.NotificationChannel;
import com.ticketstop.notification.domain.NotificationMessage;
import com.ticketstop.notification.domain.NotificationStatus;

public record NotificationResponse(UUID id, String userId, UUID orderId, NotificationChannel channel, String recipient,
                                   String subject, String body, NotificationStatus status, OffsetDateTime createdAt,
                                   OffsetDateTime deliveredAt) {
    public static NotificationResponse from(NotificationMessage message) {
        return new NotificationResponse(message.getId(), message.getUserId(), message.getOrderId(), message.getChannel(),
            message.getRecipient(), message.getSubject(), message.getBody(), message.getStatus(),
            message.getCreatedAt(), message.getDeliveredAt());
    }
}
