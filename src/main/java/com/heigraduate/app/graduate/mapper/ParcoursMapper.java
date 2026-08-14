package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.ParcoursResponse;
import com.heigraduate.app.model.Parcours;

public final class ParcoursMapper {

  private ParcoursMapper() {}

  public static ParcoursResponse toResponse(Parcours parcours) {
    return new ParcoursResponse(
        parcours.getId(), parcours.getCode(), parcours.getLabel(), parcours.isActive());
  }
}
