package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record AssignmentRequest(
        @NotNull UUID courseId,
        @NotNull UUID teacherId,
        @NotNull UUID academicYearId,
        @NotEmpty Set<UUID> groupIds) {}