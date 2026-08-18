package com.heigraduate.app.graduate.dto;

import java.util.Set;
import java.util.UUID;

public record AssignmentResponse(
    UUID id,
    UUID courseId,
    String courseReference,
    UUID teacherId,
    String teacherLastName,
    UUID academicYearId,
    String academicYearLabel,
    UUID semesterId,
    String semesterLabel,
    Set<GroupSummary> groups) {

  public record GroupSummary(UUID id, String reference) {}
}
