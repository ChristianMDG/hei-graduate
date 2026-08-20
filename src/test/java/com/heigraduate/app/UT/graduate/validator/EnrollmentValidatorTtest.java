package com.heigraduate.app.UT.graduate.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.repository.EnrollmentRepository;
import com.heigraduate.app.graduate.validator.EnrollmentValidator;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnrollmentValidatorTtest {

  @Mock private EnrollmentRepository enrollmentRepository;
  @InjectMocks private EnrollmentValidator enrollmentValidator;

  @Test
  void validateNoActiveEnrollment_shouldPass_whenNoActiveEnrollmentExists() {
    UUID studentId = UUID.randomUUID();
    when(enrollmentRepository.findByStudentIdAndEndDateIsNull(studentId))
        .thenReturn(Optional.empty());

    assertThatCode(() -> enrollmentValidator.validateNoActiveEnrollment(studentId))
        .doesNotThrowAnyException();
  }

  @Test
  void validateNoActiveEnrollment_shouldThrow_whenAnActiveEnrollmentAlreadyExists() {
    UUID studentId = UUID.randomUUID();
    Enrollment active =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .startDate(LocalDate.of(2023, 9, 1))
            .build();
    when(enrollmentRepository.findByStudentIdAndEndDateIsNull(studentId))
        .thenReturn(Optional.of(active));

    assertThatThrownBy(() -> enrollmentValidator.validateNoActiveEnrollment(studentId))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void validateTransferDate_shouldPass_whenEffectiveDateIsAfterCurrentStart() {
    Enrollment current =
        Enrollment.builder().id(UUID.randomUUID()).startDate(LocalDate.of(2023, 9, 1)).build();

    assertThatCode(
            () -> enrollmentValidator.validateTransferDate(current, LocalDate.of(2024, 9, 1)))
        .doesNotThrowAnyException();
  }

  @Test
  void validateTransferDate_shouldThrow_whenEffectiveDateEqualsCurrentStart() {
    Enrollment current =
        Enrollment.builder().id(UUID.randomUUID()).startDate(LocalDate.of(2023, 9, 1)).build();

    assertThatThrownBy(
            () -> enrollmentValidator.validateTransferDate(current, LocalDate.of(2023, 9, 1)))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void validateTransferDate_shouldThrow_whenEffectiveDateIsBeforeCurrentStart() {
    Enrollment current =
        Enrollment.builder().id(UUID.randomUUID()).startDate(LocalDate.of(2023, 9, 1)).build();

    assertThatThrownBy(
            () -> enrollmentValidator.validateTransferDate(current, LocalDate.of(2023, 1, 1)))
        .isInstanceOf(ConflictException.class);
  }
}
