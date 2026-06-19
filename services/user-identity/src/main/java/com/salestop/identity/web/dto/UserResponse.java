package com.salestop.identity.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.salestop.identity.domain.UserAccount;

public record UserResponse(UUID id, String email, String fullName, OffsetDateTime createdAt) {
    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getCreatedAt());
    }
}
