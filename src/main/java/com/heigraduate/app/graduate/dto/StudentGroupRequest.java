package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record StudentGroupRequest(
        @NotBlank(message = "reference is required") String reference,
        @Positive(message = "maxSize must be positive") Integer maxSize) {}