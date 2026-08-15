package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.EnrollmentResponse;
import com.heigraduate.app.graduate.model.Enrollment;

public final class EnrollmentMapper {

  private EnrollmentMapper() {}

  public static EnrollmentResponse toResponse(Enrollment enrollment) {
    return new EnrollmentResponse(
        enrollment.getId(),
        enrollment.getStudentId(),
        enrollment.getParcoursId(),
        enrollment.getStudentGroupId(),
        enrollment.getStartDate(),
        enrollment.getEndDate());
  }
}
