package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.DiplomaResponse;
import com.heigraduate.app.graduate.model.Diploma;

public final class DiplomaMapper {

  private DiplomaMapper() {}

  public static DiplomaResponse toResponse(Diploma diploma) {
    return new DiplomaResponse(
        diploma.getId(),
        diploma.getStudentId(),
        diploma.getPromotionId(),
        diploma.getParcoursId(),
        diploma.getObtainedDate(),
        diploma.getOverallAverage(),
        diploma.getRank(),
        diploma.getMention());
  }
}
