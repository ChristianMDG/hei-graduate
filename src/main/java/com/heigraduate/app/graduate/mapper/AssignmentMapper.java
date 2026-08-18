package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.AssignmentResponse;
import com.heigraduate.app.graduate.model.Assignment;
import com.heigraduate.app.graduate.model.Group;
import java.util.stream.Collectors;

public class AssignmentMapper {

  private AssignmentMapper() {}

  public static AssignmentResponse toResponse(Assignment assignment) {
    return new AssignmentResponse(
        assignment.getId(),
        assignment.getCourse().getId(),
        assignment.getCourse().getCourseReference(),
        assignment.getTeacher().getId(),
        assignment.getTeacher().getLastName(),
        assignment.getAcademicYear().getId(),
        assignment.getAcademicYear().getLabel(),
        assignment.getSemester() != null ? assignment.getSemester().getId() : null,
        assignment.getSemester() != null ? assignment.getSemester().getLabel() : null,
        assignment.getGroups().stream()
            .map(AssignmentMapper::toGroupSummary)
            .collect(Collectors.toSet()));
  }

  private static AssignmentResponse.GroupSummary toGroupSummary(Group group) {
    return new AssignmentResponse.GroupSummary(group.getId(), group.getReference());
  }
}
