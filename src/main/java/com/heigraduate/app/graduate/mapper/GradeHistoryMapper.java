package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.GradeHistoryResponse;
import com.heigraduate.app.graduate.model.GradeHistory;

public class GradeHistoryMapper {

  private GradeHistoryMapper() {}

  public static GradeHistoryResponse toResponse(GradeHistory history) {
    return new GradeHistoryResponse(
        history.getId(),
        history.getGrade().getId(),
        history.getOldValue(),
        history.getNewValue(),
        history.getReason(),
        history.getChangedByUserId(),
        history.getChangedAt());
  }
}
