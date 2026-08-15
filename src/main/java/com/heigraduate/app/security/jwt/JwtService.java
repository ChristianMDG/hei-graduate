package com.heigraduate.app.security.jwt;

import static java.time.Instant.now;

import com.heigraduate.app.graduate.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

  private final SecretKey key;
  private final long expirationMs;

  public JwtService(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationMs = expirationMs;
  }

  public String generate(User user) {
    var issuedAt = now();
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("uid", user.getId().toString())
        .claim("role", user.getRole().name())
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(issuedAt.plusMillis(expirationMs)))
        .signWith(key)
        .compact();
  }

  public String extractUsername(String token) {
    return parse(token).getPayload().getSubject();
  }

  public UUID extractUserId(String token) {
    return UUID.fromString(parse(token).getPayload().get("uid", String.class));
  }

  public boolean isValid(String token, String username) {
    try {
      var claims = parse(token).getPayload();
      return claims.getSubject().equals(username) && claims.getExpiration().after(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  private Jws<Claims> parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
  }
}
