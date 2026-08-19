package com.heigraduate.app.UT.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heigraduate.app.security.exception.RestAccessDeniedHandler;
import com.heigraduate.app.security.exception.RestAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

class SecurityComponentsTest {

  private ObjectMapper mapper;
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/protected");
  }

  @Test
  void commence_returnsUnauthorizedJson() throws Exception {
    RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(mapper);
    MockHttpServletResponse response = new MockHttpServletResponse();

    entryPoint.commence(request, response, new BadCredentialsException("Unauthenticated"));

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).isEqualTo("application/json");
    assertThat(response.getContentAsString())
        .contains("Authentication is required to access this resource");
  }

  @Test
  void handle_returnsForbiddenJson() throws Exception {
    RestAccessDeniedHandler accessDeniedHandler = new RestAccessDeniedHandler(mapper);
    MockHttpServletResponse response = new MockHttpServletResponse();

    accessDeniedHandler.handle(request, response, new AccessDeniedException("Forbidden"));

    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(response.getContentType()).isEqualTo("application/json");
    assertThat(response.getContentAsString())
        .contains("You do not have permission to access this resource");
  }
}
