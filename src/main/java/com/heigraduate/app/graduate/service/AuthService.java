package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.repository.UserRepository;
import com.heigraduate.app.security.jwt.JwtService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final JwtService jwtService;

  public LoginResult login(String email, String rawPassword) {
    var authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, rawPassword));
    var user = (User) authentication.getPrincipal();

    user.setLastLogin(Instant.now());
    userRepository.save(user);

    return new LoginResult(jwtService.generate(user), user);
  }

  public record LoginResult(String token, User user) {}
}
