package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.UUID;

public record StudentRequest(
    @NotNull(message = "userId is required") UUID userId,
    @NotBlank(message = "studentNumber is required") String studentNumber,
    @NotBlank(message = "lastName is required") String lastName,
    @NotBlank(message = "firstName is required") String firstName,
    @PastOrPresent(message = "birthDate must be in the past") LocalDate birthDate,
    @NotNull(message = "enrollmentDate is required") LocalDate enrollmentDate,
    @NotBlank(message = "status is required") String status) {}
