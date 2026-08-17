package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.DiplomaResponse;
import com.heigraduate.app.graduate.model.Diploma;
import com.heigraduate.app.graduate.model.Student;

public final class DiplomaMapper {

  private DiplomaMapper() {}

  public static DiplomaResponse toResponse(Diploma diploma, Student student) {
    return new DiplomaResponse(
        diploma.getId(),
        diploma.getStudentId(),
        student.getStudentNumber(),
        student.getLastName(),
        student.getFirstName(),
        diploma.getPromotionId(),
        diploma.getParcoursId(),
        diploma.getObtainedDate(),
        diploma.getOverallAverage(),
        diploma.getRank(),
        diploma.getMention());
  }
}
