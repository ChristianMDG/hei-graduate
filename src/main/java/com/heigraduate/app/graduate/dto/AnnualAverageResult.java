package com.heigraduate.app.graduate.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AnnualAverageResult(
    UUID studentId,
    UUID academicYearId,
    BigDecimal average,
    int obtainedCredits,
    int expectedCredits,
    List<UUID> validatedCourseIds,
    List<UUID> notValidatedCourseIds,
    List<UUID> missingGradeCourseIds) {

  public boolean isYearValidated() {
    return obtainedCredits >= expectedCredits;
  }
}
