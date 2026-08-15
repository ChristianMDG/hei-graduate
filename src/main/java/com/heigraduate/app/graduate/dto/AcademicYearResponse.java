package com.heigraduate.app.graduate.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AcademicYearResponse(
    UUID id, String label, LocalDate startDate, LocalDate endDate, String level) {}
