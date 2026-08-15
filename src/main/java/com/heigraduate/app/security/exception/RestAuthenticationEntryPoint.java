package com.heigraduate.app.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heigraduate.app.graduate.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper mapper;

  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
      throws IOException {
    var status = HttpStatus.UNAUTHORIZED;
    var error =
        new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            "Authentication is required to access this resource",
            request.getRequestURI());
    response.setStatus(status.value());
    response.setContentType("application/json");
    response.getWriter().write(mapper.writeValueAsString(error));
  }
}
