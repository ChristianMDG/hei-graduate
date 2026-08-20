package com.heigraduate.app.UT.graduate.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.repository.PromotionRepository;
import com.heigraduate.app.graduate.validator.PromotionValidator;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromotionValidatorTest {

  @Mock private PromotionRepository promotionRepository;
  @InjectMocks private PromotionValidator promotionValidator;

  @Test
  void validateFinalAcademicYearIsFree_shouldPass_whenNoPromotionUsesThatYearYet() {
    UUID academicYearId = UUID.randomUUID();
    when(promotionRepository.existsByFinalAcademicYearId(academicYearId)).thenReturn(false);

    assertThatCode(() -> promotionValidator.validateFinalAcademicYearIsFree(academicYearId))
        .doesNotThrowAnyException();
  }

  @Test
  void validateFinalAcademicYearIsFree_shouldThrow_whenAPromotionAlreadyUsesThatYear() {
    UUID academicYearId = UUID.randomUUID();
    when(promotionRepository.existsByFinalAcademicYearId(academicYearId)).thenReturn(true);

    assertThatThrownBy(() -> promotionValidator.validateFinalAcademicYearIsFree(academicYearId))
        .isInstanceOf(ConflictException.class);
  }
}
