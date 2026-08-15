package com.heigraduate.app.graduate.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID studentId,
        UUID parcoursId,
        UUID studentGroupId,
        LocalDate startDate,
        LocalDate endDate) {}