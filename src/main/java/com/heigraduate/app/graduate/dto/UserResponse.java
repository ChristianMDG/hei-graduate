package com.heigraduate.app.graduate.dto;

import com.heigraduate.app.graduate.model.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id, String email, UserRole role, boolean active, Instant createdAt, Instant lastLogin) {}