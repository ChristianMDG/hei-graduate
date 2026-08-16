package com.heigraduate.app.graduate.dto;

import java.util.UUID;

public record CourseTrackResponse(
    UUID id,
    UUID courseId,
    String courseReference,
    UUID trackId,
    String trackCode,
    UUID academicYearId,
    String academicYearLabel,
    Boolean obligatory) {}
