package com.heigraduate.app.graduate.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ExamResponse(
    UUID id,
    UUID courseId,
    String courseReference,
    UUID academicYearId,
    String academicYearLabel,
    String label,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    Integer coefficientNumerator,
    Integer coefficientDenominator) {}
