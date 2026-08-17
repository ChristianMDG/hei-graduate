package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.CourseTrackResponse;
import com.heigraduate.app.graduate.model.CourseTrack;

public class CourseTrackMapper {

  private CourseTrackMapper() {}

  public static CourseTrackResponse toResponse(CourseTrack ct) {
    return new CourseTrackResponse(
        ct.getId(),
        ct.getCourse().getId(),
        ct.getCourse().getCourseReference(),
        ct.getTrack().getId(),
        ct.getTrack().getCode(),
        ct.getSemester().getId(),
        ct.getSemester().getLabel(),
        ct.getSemester().getAcademicYear().getId(),
        ct.getSemester().getAcademicYear().getLabel(),
        ct.getMandatory());
  }
}
