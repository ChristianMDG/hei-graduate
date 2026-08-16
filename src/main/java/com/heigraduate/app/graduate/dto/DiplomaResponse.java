package com.heigraduate.app.graduate.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DiplomaResponse(
        UUID id,
        UUID studentId,
        UUID promotionId,
        UUID parcoursId,
        LocalDate obtainedDate,
        BigDecimal overallAverage,
        Integer rank,
        String mention) {}