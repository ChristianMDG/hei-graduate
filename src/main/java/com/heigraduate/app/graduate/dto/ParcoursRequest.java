package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParcoursRequest(
    @NotBlank(message = "code is required")
        @Size(max = 10, message = "code must be at most 10 characters")
        String code,
    @NotBlank(message = "label is required") String label) {}
