package ru.lottery.security;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Singleton;

import org.reactivestreams.Publisher;

import ru.lottery.model.RefreshToken;
import ru.lottery.model.User;
import ru.lottery.repository.RefreshTokenRepository;
import ru.lottery.repository.UserRepository;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.errors.IssuingAnAccessTokenErrorCode;
import io.micronaut.security.errors.OauthErrorResponseException;
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent;
import io.micronaut.security.token.refresh.RefreshTokenPersistence;

@Singleton
public class AppRefreshTokenPersistence implements RefreshTokenPersistence {

  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final long refreshTtlSeconds;

  public AppRefreshTokenPersistence(
      RefreshTokenRepository refreshTokenRepository,
      UserRepository userRepository,
      @Value("${jwt.refresh-ttl-seconds:604800}") long refreshTtlSeconds) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
    this.refreshTtlSeconds = refreshTtlSeconds;
  }

  @Override
  public void persistToken(RefreshTokenGeneratedEvent event) {
    String email = event.getAuthentication().getName();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new OauthErrorResponseException(
                        IssuingAnAccessTokenErrorCode.INVALID_GRANT, "User not found", null));

    RefreshToken token = new RefreshToken();
    token.setUser(user);
    token.setToken(event.getRefreshToken());
    token.setRevoked(false);
    token.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTtlSeconds));
    refreshTokenRepository.save(token);
  }

  @Override
  public Publisher<Authentication> getAuthentication(String refreshToken) {
    Optional<RefreshToken> stored =
        refreshTokenRepository
            .findByTokenAndRevokedFalse(refreshToken)
            .filter(t -> t.getExpiresAt() != null && t.getExpiresAt().isAfter(LocalDateTime.now()));

    if (stored.isEmpty()) {
      return Publishers.just(
          new OauthErrorResponseException(
              IssuingAnAccessTokenErrorCode.INVALID_GRANT,
              "Refresh token is revoked or expired",
              null));
    }

    User user = stored.get().getUser();
    return Publishers.just(
        Authentication.build(user.getEmail(), List.of("ROLE_" + user.getRole().name())));
  }
}
