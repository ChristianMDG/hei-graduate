package com.heigraduate.app.security.filter;

import com.heigraduate.app.graduate.service.UserService;
import com.heigraduate.app.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class BearerAuthFilter extends OncePerRequestFilter {

  private static final String HEADER = "Authorization";
  private static final String PREFIX = "Bearer ";

  private final JwtService jwtService;
  private final UserService userService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var header = request.getHeader(HEADER);

    if (header != null && header.startsWith(PREFIX)) {
      var token = header.substring(PREFIX.length());
      var username = safeExtractUsername(token);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        try {
          var user = userService.loadUserByUsername(username);

          if (jwtService.isValid(token, username)) {
            var authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
          }
        } catch (Exception ignored) {
          // leave the SecurityContext unauthenticated; the entry point will produce the 401
        }
      }
    }

    chain.doFilter(request, response);
  }

  private String safeExtractUsername(String token) {
    try {
      return jwtService.extractUsername(token);
    } catch (Exception e) {
      return null;
    }
  }
}
