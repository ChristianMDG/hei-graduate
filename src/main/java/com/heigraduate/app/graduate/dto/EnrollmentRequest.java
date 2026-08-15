package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/** Initial enrollment — a brand new student entering a parcours/group for the first time. */
public record EnrollmentRequest(
        @NotNull(message = "studentId is required") UUID studentId,
        @NotNull(message = "parcoursId is required") UUID parcoursId,
        @NotNull(message = "studentGroupId is required") UUID studentGroupId,
        @NotNull(message = "startDate is required") LocalDate startDate) {}