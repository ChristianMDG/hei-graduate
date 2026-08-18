package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ExamRequest(
    @NotNull UUID courseId,
    @NotNull UUID academicYearId,
    @NotNull UUID semesterId,
    @NotBlank String label,
    @NotNull LocalDate date,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @NotNull @Positive Integer coefficientNumerator,
    @NotNull @Positive Integer coefficientDenominator) {}
