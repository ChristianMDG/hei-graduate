package com.heigraduate.app.UT.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.model.UserRole;
import com.heigraduate.app.security.jwt.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private static final String SECRET = "test-only-secret-key-with-at-least-32-bytes!!";

  private JwtService jwtService;
  private User user;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(SECRET, 3600000L);
    user =
        User.builder()
            .id(UUID.randomUUID())
            .email("student@hei.school")
            .password("irrelevant-here")
            .role(UserRole.STUDENT)
            .active(true)
            .build();
  }

  @Test
  void generates_a_token_whose_subject_is_the_user_email() {
    var token = jwtService.generate(user);

    assertThat(jwtService.extractUsername(token)).isEqualTo(user.getEmail());
  }

  @Test
  void generated_token_carries_the_user_id_as_a_claim() {
    var token = jwtService.generate(user);

    assertThat(jwtService.extractUserId(token)).isEqualTo(user.getId());
  }

  @Test
  void a_freshly_generated_token_is_valid_for_its_own_username() {
    var token = jwtService.generate(user);

    assertThat(jwtService.isValid(token, user.getEmail())).isTrue();
  }

  @Test
  void a_token_is_not_valid_for_a_different_username() {
    var token = jwtService.generate(user);

    assertThat(jwtService.isValid(token, "someone-else@hei.school")).isFalse();
  }

  @Test
  void a_token_signed_with_a_different_secret_is_rejected() {
    var otherService = new JwtService("another-completely-different-secret-key-32b", 3600000L);
    var token = otherService.generate(user);

    assertThat(jwtService.isValid(token, user.getEmail())).isFalse();
  }

  @Test
  void an_already_expired_token_is_not_valid() {
    var expiredService = new JwtService(SECRET, -1000L);
    var token = expiredService.generate(user);

    assertThat(jwtService.isValid(token, user.getEmail())).isFalse();
  }
}
