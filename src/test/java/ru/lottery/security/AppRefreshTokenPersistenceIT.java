package ru.lottery.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import ru.lottery.model.RefreshToken;
import ru.lottery.model.User;
import ru.lottery.repository.RefreshTokenRepository;
import ru.lottery.support.AbstractIntegrationTest;

import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.errors.OauthErrorResponseException;
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent;
import reactor.core.publisher.Mono;

class AppRefreshTokenPersistenceIT extends AbstractIntegrationTest {

  @Inject AppRefreshTokenPersistence persistence;
  @Inject RefreshTokenRepository refreshTokenRepository;

  private RefreshTokenGeneratedEvent eventFor(String email, String token) {
    Authentication auth = Authentication.build(email, List.of("ROLE_USER"));
    RefreshTokenGeneratedEvent event = mock(RefreshTokenGeneratedEvent.class);
    when(event.getAuthentication()).thenReturn(auth);
    when(event.getRefreshToken()).thenReturn(token);
    return event;
  }

  @Test
  void persistTokenStoresRowAndReadbackAuthentication() {
    seedUser("p@b", "pw");
    persistence.persistToken(eventFor("p@b", "raw-token"));

    assertThat(refreshTokenRepository.findByTokenAndRevokedFalse("raw-token")).isPresent();

    Authentication authentication =
        Mono.from(persistence.getAuthentication("raw-token")).block(Duration.ofSeconds(2));
    assertThat(authentication).isNotNull();
    assertThat(authentication.getName()).isEqualTo("p@b");
    assertThat(authentication.getRoles()).contains("ROLE_USER");
  }

  @Test
  void persistTokenForUnknownUserThrowsOauthError() {
    assertThatThrownBy(() -> persistence.persistToken(eventFor("ghost@b", "raw")))
        .isInstanceOf(OauthErrorResponseException.class);
  }

  @Test
  void getAuthenticationForRevokedTokenSignalsError() {
    User user = seedUser("rev@b", "pw");
    RefreshToken token = new RefreshToken();
    token.setUser(user);
    token.setToken("raw-2");
    token.setRevoked(true);
    token.setExpiresAt(LocalDateTime.now().plusHours(1));
    refreshTokenRepository.save(token);

    assertThatThrownBy(
            () -> Mono.from(persistence.getAuthentication("raw-2")).block(Duration.ofSeconds(2)))
        .isInstanceOf(OauthErrorResponseException.class);
  }

  @Test
  void getAuthenticationForExpiredTokenSignalsError() {
    User user = seedUser("exp@b", "pw");
    RefreshToken token = new RefreshToken();
    token.setUser(user);
    token.setToken("raw-3");
    token.setRevoked(false);
    token.setExpiresAt(LocalDateTime.now().minusHours(1));
    refreshTokenRepository.save(token);

    assertThatThrownBy(
            () -> Mono.from(persistence.getAuthentication("raw-3")).block(Duration.ofSeconds(2)))
        .isInstanceOf(OauthErrorResponseException.class);
  }

  @Test
  void getAuthenticationForMissingTokenSignalsError() {
    assertThatThrownBy(
            () -> Mono.from(persistence.getAuthentication("nope")).block(Duration.ofSeconds(2)))
        .isInstanceOf(OauthErrorResponseException.class);
  }
}
