package com.heigraduate.app.graduate.dto;


import com.heigraduate.app.graduate.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotBlank @Email String email, @NotNull UserRole role, boolean active) {}