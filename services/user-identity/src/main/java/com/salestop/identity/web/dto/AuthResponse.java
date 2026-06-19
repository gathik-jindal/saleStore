package com.salestop.identity.web.dto;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, UserResponse user) {}
