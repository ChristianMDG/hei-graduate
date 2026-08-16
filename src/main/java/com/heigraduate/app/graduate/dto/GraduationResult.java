package com.heigraduate.app.graduate.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GraduationResult(
    UUID studentId,
    boolean graduated,
    BigDecimal overallAverage,
    List<UUID> unvalidatedMandatoryCourseIds,
    List<UUID> academicYearIdsConsidered) {}
