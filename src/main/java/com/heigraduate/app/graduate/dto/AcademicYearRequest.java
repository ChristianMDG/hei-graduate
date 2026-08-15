package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AcademicYearRequest(
        @NotBlank(message = "label is required") String label,
        @NotNull(message = "startDate is required") LocalDate startDate,
        @NotNull(message = "endDate is required") LocalDate endDate,
        @NotBlank(message = "level is required") String level) {}