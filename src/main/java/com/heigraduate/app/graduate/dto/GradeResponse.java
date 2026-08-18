package com.heigraduate.app.graduate.dto;

import com.heigraduate.app.graduate.model.GradeStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record GradeResponse(
    UUID id,
    UUID studentId,
    String studentLastName,
    UUID examId,
    String examLabel,
    UUID courseId,
    String courseReference,
    BigDecimal value,
    GradeStatus status,
    UUID enteredByUserId) {}
