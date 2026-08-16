package com.heigraduate.app.graduate.dto;

import java.util.UUID;

public record PromotionResponse(UUID id, String label, UUID finalAcademicYearId, String finalAcademicYearLabel) {}