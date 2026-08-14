package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CourseRequest(
    @NotBlank String courseReference,
    @NotBlank String title,
    @NotNull @Positive Integer credits) {}
