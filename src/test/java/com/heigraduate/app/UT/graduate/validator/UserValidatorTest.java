package com.heigraduate.app.UT.graduate.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.repository.UserRepository;
import com.heigraduate.app.graduate.validator.UserValidator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

  @Mock private UserRepository userRepository;
  @InjectMocks private UserValidator userValidator;

  @Test
  void validateEmailIsUnique_shouldPass_whenEmailIsFree() {
    when(userRepository.existsByEmail("nouveau@hei.mg")).thenReturn(false);

    assertThatCode(() -> userValidator.validateEmailIsUnique("nouveau@hei.mg"))
        .doesNotThrowAnyException();
  }

  @Test
  void validateEmailIsUnique_shouldThrow_whenEmailAlreadyTaken() {
    when(userRepository.existsByEmail("existant@hei.mg")).thenReturn(true);

    assertThatThrownBy(() -> userValidator.validateEmailIsUnique("existant@hei.mg"))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void validateEmailIsUniqueForUpdate_shouldPass_whenNoUserHasThatEmail() {
    when(userRepository.findByEmail("libre@hei.mg")).thenReturn(Optional.empty());

    assertThatCode(
            () -> userValidator.validateEmailIsUniqueForUpdate("libre@hei.mg", UUID.randomUUID()))
        .doesNotThrowAnyException();
  }

  @Test
  void validateEmailIsUniqueForUpdate_shouldPass_whenEmailBelongsToTheUserItself() {
    UUID selfId = UUID.randomUUID();
    User self = User.builder().id(selfId).email("moi@hei.mg").build();
    when(userRepository.findByEmail("moi@hei.mg")).thenReturn(Optional.of(self));

    assertThatCode(() -> userValidator.validateEmailIsUniqueForUpdate("moi@hei.mg", selfId))
        .doesNotThrowAnyException();
  }

  @Test
  void validateEmailIsUniqueForUpdate_shouldThrow_whenEmailBelongsToAnotherUser() {
    User other = User.builder().id(UUID.randomUUID()).email("autre@hei.mg").build();
    when(userRepository.findByEmail("autre@hei.mg")).thenReturn(Optional.of(other));

    assertThatThrownBy(
            () -> userValidator.validateEmailIsUniqueForUpdate("autre@hei.mg", UUID.randomUUID()))
        .isInstanceOf(ConflictException.class);
  }
}
