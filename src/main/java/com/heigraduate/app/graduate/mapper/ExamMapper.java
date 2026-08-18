package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.ExamResponse;
import com.heigraduate.app.graduate.model.Exam;

public class ExamMapper {

  private ExamMapper() {}

  public static ExamResponse toResponse(Exam exam) {
    return new ExamResponse(
        exam.getId(),
        exam.getCourse().getId(),
        exam.getCourse().getCourseReference(),
        exam.getAcademicYear().getId(),
        exam.getAcademicYear().getLabel(),
        exam.getSemester().getId(),
        exam.getSemester().getLabel(),
        exam.getLabel(),
        exam.getDate(),
        exam.getStartTime(),
        exam.getEndTime(),
        exam.getCoefficientNumerator(),
        exam.getCoefficientDenominator());
  }
}
