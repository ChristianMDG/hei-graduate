package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TeacherRequest(
    @NotNull(message = "userId is required") UUID userId,
    @NotBlank(message = "lastName is required") String lastName,
    @NotBlank(message = "firstName is required") String firstName,
    String specialty,
    String contractType) {}
