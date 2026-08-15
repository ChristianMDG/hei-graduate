package com.heigraduate.app.graduate.dto;

import java.time.LocalDate;
import java.util.UUID;

public record StudentResponse(
        UUID id,
        UUID userId,
        String studentNumber,
        String lastName,
        String firstName,
        LocalDate birthDate,
        LocalDate enrollmentDate,
        String status) {}