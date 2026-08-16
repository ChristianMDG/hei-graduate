package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ExamRequest(
    @NotNull UUID courseId,
    @NotNull UUID academicYearId,
    @NotBlank String label,
    @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal coefficient) {}
