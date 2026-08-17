package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.UUID;

public record SemesterRequest(
    @NotNull(message = "academicYearId is required") UUID academicYearId,
    @NotBlank String label,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @Positive Integer expectedCredits) {}
