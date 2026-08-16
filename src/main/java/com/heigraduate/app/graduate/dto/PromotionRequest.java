package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PromotionRequest(
        @NotBlank(message = "label is required") String label,
        @NotNull(message = "finalAcademicYearId is required") UUID finalAcademicYearId) {}