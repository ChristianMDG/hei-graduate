package com.heigraduate.app.graduate.validator;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.repository.PromotionRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionValidator {

  private final PromotionRepository promotionRepository;

  public void validateFinalAcademicYearIsFree(UUID academicYearId) {
    if (promotionRepository.existsByFinalAcademicYearId(academicYearId)) {
      throw new ConflictException(
          "A promotion already exists for academic year: " + academicYearId);
    }
  }
}
