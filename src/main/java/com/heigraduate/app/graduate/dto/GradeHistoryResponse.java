package com.heigraduate.app.graduate.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GradeHistoryResponse(
    UUID id,
    UUID gradeId,
    BigDecimal oldValue,
    BigDecimal newValue,
    String reason,
    UUID changedByUserId,
    LocalDateTime changedAt) {}
