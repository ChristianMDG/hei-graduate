package com.heigraduate.app.graduate.dto;

import java.util.UUID;

public record CourseTrackResponse(
    UUID id,
    UUID courseId,
    String courseReference,
    UUID trackId,
    String trackCode,
    UUID semesterId,
    String semesterLabel,
    UUID academicYearId,
    String academicYearLabel,
    Boolean mandatory) {}
