package com.heigraduate.app.graduate.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ExamResponse(
    UUID id,
    UUID courseId,
    String courseReference,
    UUID academicYearId,
    String academicYearLabel,
    UUID semesterId,
    String semesterLabel,
    String label,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    BigDecimal coefficient) {}
