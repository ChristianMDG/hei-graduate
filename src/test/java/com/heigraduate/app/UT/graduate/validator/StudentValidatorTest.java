package com.heigraduate.app.UT.graduate.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.repository.StudentRepository;
import com.heigraduate.app.graduate.validator.StudentValidator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentValidatorTest {

  @Mock private StudentRepository studentRepository;
  @InjectMocks private StudentValidator studentValidator;

  @Test
  void validateStudentNumberUniqueness_shouldPass_whenNumberIsFree() {
    when(studentRepository.findByStudentNumberIgnoreCase("STD24001")).thenReturn(Optional.empty());

    assertThatCode(() -> studentValidator.validateStudentNumberUniqueness("STD24001", null))
        .doesNotThrowAnyException();
  }

  @Test
  void validateStudentNumberUniqueness_shouldThrow_whenNumberUsedByAnotherStudent() {
    Student other = Student.builder().id(UUID.randomUUID()).studentNumber("STD24001").build();
    when(studentRepository.findByStudentNumberIgnoreCase("STD24001"))
        .thenReturn(Optional.of(other));

    assertThatThrownBy(
            () -> studentValidator.validateStudentNumberUniqueness("STD24001", UUID.randomUUID()))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void validateStudentNumberUniqueness_shouldPass_whenNumberBelongsToStudentBeingUpdated() {
    UUID selfId = UUID.randomUUID();
    Student self = Student.builder().id(selfId).studentNumber("STD24001").build();
    when(studentRepository.findByStudentNumberIgnoreCase("STD24001")).thenReturn(Optional.of(self));

    assertThatCode(() -> studentValidator.validateStudentNumberUniqueness("STD24001", selfId))
        .doesNotThrowAnyException();
  }

  @Test
  void validateUserNotAlreadyLinked_shouldPass_whenUserHasNoStudentProfileYet() {
    UUID userId = UUID.randomUUID();
    when(studentRepository.findByUserId(userId)).thenReturn(Optional.empty());

    assertThatCode(() -> studentValidator.validateUserNotAlreadyLinked(userId, null))
        .doesNotThrowAnyException();
  }

  @Test
  void validateUserNotAlreadyLinked_shouldThrow_whenUserAlreadyLinkedToAnotherStudent() {
    UUID userId = UUID.randomUUID();
    Student other = Student.builder().id(UUID.randomUUID()).userId(userId).build();
    when(studentRepository.findByUserId(userId)).thenReturn(Optional.of(other));

    assertThatThrownBy(
            () -> studentValidator.validateUserNotAlreadyLinked(userId, UUID.randomUUID()))
        .isInstanceOf(ConflictException.class);
  }
}
