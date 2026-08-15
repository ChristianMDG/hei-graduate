package com.heigraduate.app.UT.security.authorizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.model.UserRole;
import java.util.Map;
import java.util.UUID;

import com.heigraduate.app.security.authorizer.SelfOrAdminAuthorizationManager;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

class SelfOrAdminAuthorizationManagerTest {

  private final SelfOrAdminAuthorizationManager manager = new SelfOrAdminAuthorizationManager();

  @Test
  void grants_access_when_principal_is_admin_even_for_someone_elses_id() {
    var admin = userWith(UserRole.ADMIN);
    var context = contextWithId(UUID.randomUUID().toString());

    var decision = manager.check(authenticated(admin), context);

    assertThat(decision.isGranted()).isTrue();
  }

  @Test
  void grants_access_when_the_path_id_matches_the_principals_own_id() {
    var student = userWith(UserRole.STUDENT);
    var context = contextWithId(student.getId().toString());

    var decision = manager.check(authenticated(student), context);

    assertThat(decision.isGranted()).isTrue();
  }

  @Test
  void denies_access_when_a_student_requests_another_students_id() {
    var student = userWith(UserRole.STUDENT);
    var context = contextWithId(UUID.randomUUID().toString());

    var decision = manager.check(authenticated(student), context);

    assertThat(decision.isGranted()).isFalse();
  }

  @Test
  void denies_access_when_there_is_no_authenticated_principal() {
    Authentication unauthenticated = mock(Authentication.class);
    when(unauthenticated.isAuthenticated()).thenReturn(false);
    var context = contextWithId(UUID.randomUUID().toString());

    var decision = manager.check(() -> unauthenticated, context);

    assertThat(decision.isGranted()).isFalse();
  }

  private static User userWith(UserRole role) {
    return User.builder()
        .id(UUID.randomUUID())
        .email(role.name().toLowerCase() + "@hei.school")
        .password("irrelevant-here")
        .role(role)
        .active(true)
        .build();
  }

  private static java.util.function.Supplier<Authentication> authenticated(User user) {
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(user);
    return () -> authentication;
  }

  private static RequestAuthorizationContext contextWithId(String id) {
    return new RequestAuthorizationContext(new MockHttpServletRequest(), Map.of("id", id));
  }
}
