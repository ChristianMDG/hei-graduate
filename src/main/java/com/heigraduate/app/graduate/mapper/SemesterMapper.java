package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.SemesterResponse;
import com.heigraduate.app.graduate.model.Semester;

public class SemesterMapper {

  private SemesterMapper() {}

  public static SemesterResponse toResponse(Semester semester) {
    return new SemesterResponse(
        semester.getId(),
        semester.getAcademicYear().getId(),
        semester.getAcademicYear().getLabel(),
        semester.getLabel(),
        semester.getStartDate(),
        semester.getEndDate(),
        semester.getExpectedCredits(),
        semester.getActive());
  }
}
