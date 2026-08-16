package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record TransferRequest(
    UUID parcoursId,
    UUID groupId,
    @NotNull(message = "effectiveDate is required") LocalDate effectiveDate) {}
