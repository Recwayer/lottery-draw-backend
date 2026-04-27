package ru.lottery.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import ru.lottery.model.User;
import ru.lottery.model.enums.Role;

class JwtServiceTest {
  private static final String SECRET = "test-jwt-secret-with-at-least-32-bytes";

  private final JwtService jwtService = new JwtService(SECRET, 60);

  @Test
  void generateAndValidateToken() {
    UUID userId = UUID.randomUUID();
    User user = User.create("user@example.com", "hash", Role.USER);
    user.setId(userId);

    String token = jwtService.generateToken(user);
    AuthenticatedUser authenticatedUser = jwtService.parseToken(token);

    assertThat(jwtService.validateToken(token)).isTrue();
    assertThat(jwtService.getExpiresInSeconds()).isEqualTo(3600);
    assertThat(authenticatedUser.userId()).isEqualTo(userId);
    assertThat(authenticatedUser.email()).isEqualTo("user@example.com");
    assertThat(authenticatedUser.role()).isEqualTo(Role.USER);
  }

  @Test
  void invalidTokenIsRejected() {
    assertThat(jwtService.validateToken("invalid-token")).isFalse();
  }
}
