package com.heigraduate.app.graduate.dto;

import java.time.LocalDate;
import java.util.UUID;

public record SemesterResponse(
    UUID id,
    UUID academicYearId,
    String academicYearLabel,
    String label,
    LocalDate startDate,
    LocalDate endDate,
    Integer expectedCredits,
    Boolean active) {}
