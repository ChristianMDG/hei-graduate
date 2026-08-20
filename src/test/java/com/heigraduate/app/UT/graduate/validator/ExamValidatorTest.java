package com.heigraduate.app.UT.graduate.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.exception.BadRequestException;
import com.heigraduate.app.graduate.model.Exam;
import com.heigraduate.app.graduate.repository.ExamRepository;
import com.heigraduate.app.graduate.validator.ExamValidator;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExamValidatorTest {

  @Mock private ExamRepository examRepository;
  @InjectMocks private ExamValidator examValidator;

  private UUID courseId;
  private UUID semesterId;

  @BeforeEach
  void setUp() {
    courseId = UUID.randomUUID();
    semesterId = UUID.randomUUID();
  }

  private Exam examWithFraction(int numerator, int denominator) {
    return Exam.builder()
        .id(UUID.randomUUID())
        .coefficientNumerator(numerator)
        .coefficientDenominator(denominator)
        .build();
  }

  @Test
  void validateTimeRange_shouldPass_whenStartBeforeEnd() {
    assertThatCode(() -> examValidator.validateTimeRange(LocalTime.of(8, 0), LocalTime.of(10, 0)))
        .doesNotThrowAnyException();
  }

  @Test
  void validateTimeRange_shouldThrow_whenStartEqualsEnd() {
    assertThatThrownBy(
            () -> examValidator.validateTimeRange(LocalTime.of(8, 0), LocalTime.of(8, 0)))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void validateTimeRange_shouldThrow_whenStartAfterEnd() {
    assertThatThrownBy(
            () -> examValidator.validateTimeRange(LocalTime.of(11, 0), LocalTime.of(9, 0)))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void validateCoefficientFraction_shouldPass_whenNumeratorLessThanDenominator() {
    assertThatCode(() -> examValidator.validateCoefficientFraction(1, 4))
        .doesNotThrowAnyException();
  }

  @Test
  void validateCoefficientFraction_shouldPass_whenNumeratorEqualsDenominator() {
    assertThatCode(() -> examValidator.validateCoefficientFraction(1, 1))
        .doesNotThrowAnyException();
  }

  @Test
  void validateCoefficientFraction_shouldThrow_whenDenominatorIsZero() {
    assertThatThrownBy(() -> examValidator.validateCoefficientFraction(1, 0))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void validateCoefficientFraction_shouldThrow_whenNumeratorExceedsDenominator() {
    assertThatThrownBy(() -> examValidator.validateCoefficientFraction(3, 2))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void validateCoefficientSum_shouldPass_whenTotalStaysUnderOne() {
    when(examRepository.findByCourseIdAndSemesterId(courseId, semesterId))
        .thenReturn(List.of(examWithFraction(1, 2)));

    assertThatCode(() -> examValidator.validateCoefficientSum(courseId, semesterId, 1, 4, null))
        .doesNotThrowAnyException();
  }

  @Test
  void validateCoefficientSum_shouldThrow_whenTotalExceedsOne() {
    when(examRepository.findByCourseIdAndSemesterId(courseId, semesterId))
        .thenReturn(List.of(examWithFraction(1, 2), examWithFraction(1, 2)));

    assertThatThrownBy(() -> examValidator.validateCoefficientSum(courseId, semesterId, 1, 4, null))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void validateCoefficientSum_shouldExcludeTheGivenExam_whenRecomputingOnUpdate() {
    Exam beingEdited = examWithFraction(1, 2);

    when(examRepository.findByCourseIdAndSemesterId(courseId, semesterId))
        .thenReturn(List.of(beingEdited, examWithFraction(1, 2)));

    assertThatCode(
            () ->
                examValidator.validateCoefficientSum(
                    courseId, semesterId, 1, 2, beingEdited.getId()))
        .doesNotThrowAnyException();
  }

  @Test
  void validateCoefficientSumEqualsOne_shouldThrow_whenNoExamsExist() {
    when(examRepository.findByCourseIdAndSemesterId(courseId, semesterId)).thenReturn(List.of());

    assertThatThrownBy(() -> examValidator.validateCoefficientSumEqualsOne(courseId, semesterId))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void validateCoefficientSumEqualsOne_shouldPass_whenExactExampleFromSpec() {
    when(examRepository.findByCourseIdAndSemesterId(courseId, semesterId))
        .thenReturn(
            List.of(examWithFraction(1, 2), examWithFraction(1, 4), examWithFraction(1, 4)));

    assertThatCode(() -> examValidator.validateCoefficientSumEqualsOne(courseId, semesterId))
        .doesNotThrowAnyException();
  }

  @Test
  void validateCoefficientSumEqualsOne_shouldThrow_whenSumIsIncomplete() {
    when(examRepository.findByCourseIdAndSemesterId(courseId, semesterId))
        .thenReturn(List.of(examWithFraction(1, 2), examWithFraction(1, 4)));

    assertThatThrownBy(() -> examValidator.validateCoefficientSumEqualsOne(courseId, semesterId))
        .isInstanceOf(BadRequestException.class);
  }
}
