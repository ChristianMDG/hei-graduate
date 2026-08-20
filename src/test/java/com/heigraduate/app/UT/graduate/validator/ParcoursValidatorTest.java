package com.heigraduate.app.UT.graduate.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.model.Parcours;
import com.heigraduate.app.graduate.repository.ParcoursRepository;
import com.heigraduate.app.graduate.validator.ParcoursValidator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParcoursValidatorTest {

  @Mock private ParcoursRepository parcoursRepository;
  @InjectMocks private ParcoursValidator parcoursValidator;

  @Test
  void validateCodeUniqueness_shouldPass_whenCodeDoesNotExist() {
    when(parcoursRepository.findByCodeIgnoreCase("TN")).thenReturn(Optional.empty());

    assertThatCode(() -> parcoursValidator.validateCodeUniqueness("TN", null))
        .doesNotThrowAnyException();
  }

  @Test
  void validateCodeUniqueness_shouldThrow_whenCodeUsedByAnotherParcours() {
    Parcours existing = Parcours.builder().id(UUID.randomUUID()).code("EL").build();
    when(parcoursRepository.findByCodeIgnoreCase("EL")).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> parcoursValidator.validateCodeUniqueness("EL", UUID.randomUUID()))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void validateCodeUniqueness_shouldPass_whenCodeBelongsToTheParcoursBeingUpdated() {
    UUID selfId = UUID.randomUUID();
    Parcours existing = Parcours.builder().id(selfId).code("EL").build();
    when(parcoursRepository.findByCodeIgnoreCase("EL")).thenReturn(Optional.of(existing));

    assertThatCode(() -> parcoursValidator.validateCodeUniqueness("EL", selfId))
        .doesNotThrowAnyException();
  }
}
