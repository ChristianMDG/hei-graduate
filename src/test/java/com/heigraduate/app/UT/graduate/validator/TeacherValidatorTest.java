package com.heigraduate.app.UT.graduate.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.model.Teacher;
import com.heigraduate.app.graduate.repository.TeacherRepository;
import com.heigraduate.app.graduate.validator.TeacherValidator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherValidatorTest {

  @Mock private TeacherRepository teacherRepository;
  @InjectMocks private TeacherValidator teacherValidator;

  @Test
  void validateUserNotAlreadyLinked_shouldPass_whenUserHasNoTeacherProfileYet() {
    UUID userId = UUID.randomUUID();
    when(teacherRepository.findByUserId(userId)).thenReturn(Optional.empty());

    assertThatCode(() -> teacherValidator.validateUserNotAlreadyLinked(userId, null))
        .doesNotThrowAnyException();
  }

  @Test
  void validateUserNotAlreadyLinked_shouldThrow_whenUserAlreadyLinkedToAnotherTeacher() {
    UUID userId = UUID.randomUUID();
    Teacher other = Teacher.builder().id(UUID.randomUUID()).userId(userId).build();
    when(teacherRepository.findByUserId(userId)).thenReturn(Optional.of(other));

    assertThatThrownBy(
            () -> teacherValidator.validateUserNotAlreadyLinked(userId, UUID.randomUUID()))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void validateUserNotAlreadyLinked_shouldPass_whenLinkBelongsToTeacherBeingUpdated() {
    UUID userId = UUID.randomUUID();
    UUID selfId = UUID.randomUUID();
    Teacher self = Teacher.builder().id(selfId).userId(userId).build();
    when(teacherRepository.findByUserId(userId)).thenReturn(Optional.of(self));

    assertThatCode(() -> teacherValidator.validateUserNotAlreadyLinked(userId, selfId))
        .doesNotThrowAnyException();
  }
}
