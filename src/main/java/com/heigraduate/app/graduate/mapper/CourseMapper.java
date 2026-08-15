package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.CourseResponse;
import com.heigraduate.app.graduate.model.Course;

public final class CourseMapper {

  private CourseMapper() {}

  public static CourseResponse toResponse(Course course) {
    return new CourseResponse(
        course.getId(),
        course.getCourseReference(),
        course.getTitle(),
        course.getCredits(),
        course.getActive());
  }
}
