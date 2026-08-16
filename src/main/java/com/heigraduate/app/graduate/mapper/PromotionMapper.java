package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.PromotionResponse;
import com.heigraduate.app.graduate.model.Promotion;

public final class PromotionMapper {

  private PromotionMapper() {}

  public static PromotionResponse toResponse(Promotion promotion) {
    return new PromotionResponse(
        promotion.getId(),
        promotion.getLabel(),
        promotion.getFinalAcademicYear().getId(),
        promotion.getFinalAcademicYear().getLabel());
  }
}
