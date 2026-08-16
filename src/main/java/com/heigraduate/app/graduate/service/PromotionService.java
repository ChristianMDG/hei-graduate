package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.PromotionRequest;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Promotion;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.PromotionRepository;
import com.heigraduate.app.graduate.validator.PromotionValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromotionService {

  private final PromotionRepository promotionRepository;
  private final AcademicYearRepository academicYearRepository;
  private final PromotionValidator promotionValidator;

  @Transactional
  public Promotion create(PromotionRequest request) {
    promotionValidator.validateFinalAcademicYearIsFree(request.finalAcademicYearId());
    AcademicYear finalYear =
        academicYearRepository
            .findById(request.finalAcademicYearId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "AcademicYear not found: " + request.finalAcademicYearId()));

    Promotion promotion =
        Promotion.builder().label(request.label()).finalAcademicYear(finalYear).build();
    return promotionRepository.save(promotion);
  }

  @Transactional(readOnly = true)
  public Promotion getById(UUID id) {
    return promotionRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Promotion not found: " + id));
  }

  @Transactional(readOnly = true)
  public List<Promotion> getAll() {
    return promotionRepository.findAll();
  }
}
