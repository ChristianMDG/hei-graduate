package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.StudentGroupResponse;
import com.heigraduate.app.graduate.model.StudentGroup;

public final class StudentGroupMapper {

  private StudentGroupMapper() {}

  public static StudentGroupResponse toResponse(StudentGroup studentGroup) {
    return new StudentGroupResponse(
        studentGroup.getId(), studentGroup.getReference(), studentGroup.getMaxSize());
  }
}
