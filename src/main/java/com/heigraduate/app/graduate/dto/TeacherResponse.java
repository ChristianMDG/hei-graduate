package com.heigraduate.app.graduate.dto;

import java.util.UUID;

public record TeacherResponse(
    UUID id,
    UUID userId,
    String lastName,
    String firstName,
    String specialty,
    String contractType) {}
