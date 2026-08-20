package com.heigraduate.app.UT.graduate.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.validator.AcademicYearValidator;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class AcademicYearValidatorTest {

  private final AcademicYearValidator academicYearValidator = new AcademicYearValidator();

  @Test
  void validateDateRange_shouldPass_whenStartBeforeEnd() {
    assertThatCode(
            () ->
                academicYearValidator.validateDateRange(
                    LocalDate.of(2023, 9, 1), LocalDate.of(2024, 6, 30)))
        .doesNotThrowAnyException();
  }

  @Test
  void validateDateRange_shouldThrow_whenStartEqualsEnd() {
    LocalDate date = LocalDate.of(2023, 9, 1);
    assertThatThrownBy(() -> academicYearValidator.validateDateRange(date, date))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void validateDateRange_shouldThrow_whenStartAfterEnd() {
    assertThatThrownBy(
            () ->
                academicYearValidator.validateDateRange(
                    LocalDate.of(2024, 6, 30), LocalDate.of(2023, 9, 1)))
        .isInstanceOf(ConflictException.class);
  }
}
