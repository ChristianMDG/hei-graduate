package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record GroupRequest(@NotBlank String reference, @Positive Integer capacity) {}
