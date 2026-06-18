package com.ticketstop.notification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketstop.notification.domain.NotificationMessage;

public interface NotificationRepository extends JpaRepository<NotificationMessage, UUID> {
    List<NotificationMessage> findByUserIdOrderByCreatedAtDesc(String userId);
}
