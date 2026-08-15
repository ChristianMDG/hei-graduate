package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.model.UserRole;
import com.heigraduate.app.graduate.repository.UserRepository;
import com.heigraduate.app.graduate.service.AuthService;
import com.heigraduate.app.security.jwt.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

class AuthServiceTest {

  private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
  private final UserRepository userRepository = mock(UserRepository.class);
  private final JwtService jwtService = mock(JwtService.class);

  private final AuthService authService =
      new AuthService(authenticationManager, userRepository, jwtService);

  private User user;

  @BeforeEach
  void setUp() {
    user =
        User.builder()
            .id(UUID.randomUUID())
            .email("teacher@hei.school")
            .password("hashed")
            .role(UserRole.TEACHER)
            .active(true)
            .build();
  }

  @Test
  void returns_a_token_and_refreshes_last_login_on_successful_authentication() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(user);
    when(authenticationManager.authenticate(any())).thenReturn(authentication);
    when(jwtService.generate(user)).thenReturn("a-generated-token");
    when(userRepository.save(user)).thenReturn(user);

    var result = authService.login(user.getEmail(), "the-raw-password");

    assertThat(result.token()).isEqualTo("a-generated-token");
    assertThat(result.user()).isEqualTo(user);
    assertThat(user.getLastLogin()).isNotNull();
    verify(userRepository).save(user);
  }

  @Test
  void propagates_the_authentication_manager_failure_without_swallowing_it() {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("bad credentials"));

    org.junit.jupiter.api.Assertions.assertThrows(
        BadCredentialsException.class, () -> authService.login(user.getEmail(), "wrong-password"));
  }
}
