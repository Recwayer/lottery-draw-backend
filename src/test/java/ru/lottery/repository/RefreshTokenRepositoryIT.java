package ru.lottery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import ru.lottery.model.RefreshToken;
import ru.lottery.model.User;
import ru.lottery.support.AbstractIntegrationTest;

class RefreshTokenRepositoryIT extends AbstractIntegrationTest {

  @Inject RefreshTokenRepository refreshTokenRepository;

  private RefreshToken seedToken(User user, String token, boolean revoked) {
    RefreshToken t = new RefreshToken();
    t.setUser(user);
    t.setToken(token);
    t.setRevoked(revoked);
    t.setExpiresAt(LocalDateTime.now().plusHours(1));
    return refreshTokenRepository.save(t);
  }

  @Test
  void findByTokenAndRevokedFalseFetchesUser() {
    User u = seedUser("a@b", "pw");
    seedToken(u, "raw-1", false);

    Optional<RefreshToken> found = refreshTokenRepository.findByTokenAndRevokedFalse("raw-1");
    assertThat(found).isPresent();
    assertThat(found.get().getUser()).isNotNull();
    assertThat(found.get().getUser().getEmail()).isEqualTo("a@b");
  }

  @Test
  void revokeByTokenFlipsFlag() {
    User u = seedUser("rev@b", "pw");
    seedToken(u, "tok-2", false);

    refreshTokenRepository.revokeByToken("tok-2");

    Optional<RefreshToken> found = refreshTokenRepository.findByTokenAndRevokedFalse("tok-2");
    assertThat(found).isEmpty();
  }

  @Test
  void revokeAllForUserRevokesEveryActiveToken() {
    User u = seedUser("multi@b", "pw");
    seedToken(u, "a", false);
    seedToken(u, "b", false);
    seedToken(u, "c", true);

    refreshTokenRepository.revokeAllForUser(u);

    assertThat(refreshTokenRepository.findByTokenAndRevokedFalse("a")).isEmpty();
    assertThat(refreshTokenRepository.findByTokenAndRevokedFalse("b")).isEmpty();
  }
}
