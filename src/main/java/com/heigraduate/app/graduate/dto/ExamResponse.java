package com.heigraduate.app.graduate.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ExamResponse(
    UUID id,
    UUID courseId,
    String courseReference,
    UUID academicYearId,
    String academicYearLabel,
    String label,
    BigDecimal coefficient) {}
