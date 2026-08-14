package com.heigraduate.app.graduate.dto;

import java.util.UUID;

public record CourseResponse(
    UUID id, String courseReference, String title, Integer ectsCredits, Boolean active) {}
