package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.GradeResponse;
import com.heigraduate.app.graduate.model.Grade;

public class GradeMapper {

  private GradeMapper() {}

  public static GradeResponse toResponse(Grade grade) {
    return new GradeResponse(
        grade.getId(),
        grade.getStudent().getId(),
        grade.getStudent().getLastName(),
        grade.getExam().getId(),
        grade.getExam().getLabel(),
        grade.getExam().getCourse().getId(),
        grade.getExam().getCourse().getCourseReference(),
        grade.getValue(),
        grade.getStatus());
  }
}
