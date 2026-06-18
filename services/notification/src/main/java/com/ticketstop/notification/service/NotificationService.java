package com.ticketstop.notification.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketstop.notification.domain.NotificationMessage;
import com.ticketstop.notification.dto.NotificationRequest;
import com.ticketstop.notification.dto.NotificationResponse;
import com.ticketstop.notification.repository.NotificationRepository;

@Service
public class NotificationService {
    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional
    public NotificationResponse send(NotificationRequest request) {
        NotificationMessage message = new NotificationMessage(UUID.randomUUID(), request.userId(), request.orderId(),
            request.channel(), request.recipient(), request.subject(), request.body());
        message.markDelivered();
        return NotificationResponse.from(notifications.save(message));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> forUser(String userId) {
        return notifications.findByUserIdOrderByCreatedAtDesc(userId).stream().map(NotificationResponse::from).toList();
    }
}
