package com.heigraduate.app.graduate.contract;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface FinalGradeQuery {

  Optional<BigDecimal> getFinalGrade(UUID studentId, UUID courseId);
}
