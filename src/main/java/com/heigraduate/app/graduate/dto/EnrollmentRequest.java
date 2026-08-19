package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record EnrollmentRequest(
    @NotNull(message = "studentId is required") UUID studentId,
    @NotNull(message = "parcoursId is required") UUID parcoursId,
    @NotNull(message = "groupId is required") UUID groupId,
    @NotNull(message = "startDate is required") LocalDate startDate) {}
