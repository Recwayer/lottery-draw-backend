package ru.lottery.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import ru.lottery.exception.UnauthorizedException;
import ru.lottery.model.User;
import ru.lottery.model.enums.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtService {
  private static final int MIN_SECRET_BYTES = 32;
  private static final long DEFAULT_EXPIRES_MINUTES = 60;

  private final SecretKey key;
  private final Duration expiresIn;
  private final Clock clock;

  public JwtService(String secret, long expiresMinutes) {
    this(secret, expiresMinutes, Clock.systemUTC());
  }

  public JwtService(String secret, long expiresMinutes, Clock clock) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("JWT_SECRET is required");
    }

    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (secretBytes.length < MIN_SECRET_BYTES) {
      throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
    }

    this.key = Keys.hmacShaKeyFor(secretBytes);
    this.expiresIn =
        Duration.ofMinutes(expiresMinutes > 0 ? expiresMinutes : DEFAULT_EXPIRES_MINUTES);
    this.clock = clock;
  }

  public String generateToken(User user) {
    Instant now = clock.instant();
    String userId = user.getId().toString();
    return Jwts.builder()
        .subject(userId)
        .claim("userId", userId)
        .claim("email", user.getEmail())
        .claim("role", user.getRole().name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expiresIn)))
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  public AuthenticatedUser parseToken(String token) {
    try {
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
      UUID userId = UUID.fromString(requiredClaim(claims, "userId"));
      String email = requiredClaim(claims, "email");
      Role role = Role.valueOf(requiredClaim(claims, "role"));
      return new AuthenticatedUser(userId, email, role);
    } catch (IllegalArgumentException | JwtException e) {
      throw new UnauthorizedException("Invalid or expired token");
    }
  }

  public boolean validateToken(String token) {
    try {
      parseToken(token);
      return true;
    } catch (UnauthorizedException e) {
      return false;
    }
  }

  public long getExpiresInSeconds() {
    return expiresIn.toSeconds();
  }

  private String requiredClaim(Claims claims, String name) {
    String value = claims.get(name, String.class);
    if (value == null || value.isBlank()) {
      throw new UnauthorizedException("Invalid or expired token");
    }
    return value;
  }
}
