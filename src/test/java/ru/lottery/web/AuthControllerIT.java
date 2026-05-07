package ru.lottery.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import ru.lottery.model.enums.UserEventType;
import ru.lottery.repository.RefreshTokenRepository;
import ru.lottery.repository.UserEventRepository;
import ru.lottery.support.AbstractWebIntegrationTest;
import ru.lottery.web.dto.LogoutRequest;
import ru.lottery.web.dto.RegisterRequest;

import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.token.render.BearerAccessRefreshToken;

class AuthControllerIT extends AbstractWebIntegrationTest {

  @Test
  void registerCreatesUser() {
    HttpStatus status =
        httpClient
            .toBlocking()
            .exchange(HttpRequest.POST("/register", new RegisterRequest("new@b", "password123")))
            .status();
    assertThat((Object) status).isEqualTo(HttpStatus.CREATED);
    assertThat(userRepository.findByEmail("new@b")).isPresent();
  }

  @Test
  void registerDuplicateReturnsConflict() {
    seedUser("dup@b", "password123");
    assertThatThrownBy(
            () ->
                httpClient
                    .toBlocking()
                    .exchange(
                        HttpRequest.POST("/register", new RegisterRequest("dup@b", "password123"))))
        .isInstanceOf(HttpClientResponseException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void loginAndLogoutFlow() {
    seedUser("auth@b", "password123");

    BearerAccessRefreshToken token = login("auth@b", "password123");
    assertThat(token.getAccessToken()).isNotBlank();
    assertThat(token.getRefreshToken()).isNotBlank();

    UserEventRepository userEventRepository = ctx.getBean(UserEventRepository.class);
    RefreshTokenRepository refreshTokenRepository = ctx.getBean(RefreshTokenRepository.class);

    MutableHttpRequest<LogoutRequest> req =
        HttpRequest.POST("/logout", new LogoutRequest(token.getRefreshToken()))
            .bearerAuth(token.getAccessToken());
    HttpStatus status = httpClient.toBlocking().exchange(req).status();
    assertThat((Object) status).isEqualTo(HttpStatus.NO_CONTENT);

    var user = userRepository.findByEmail("auth@b").orElseThrow();
    var logoutEvents =
        userEventRepository.findByUserIdAndTypeOrderByCreatedAtDesc(
            user.getId(), UserEventType.LOGOUT, Pageable.from(0, 5));
    assertThat(logoutEvents).hasSize(1);
    long revoked = refreshTokenRepository.count();
    assertThat(revoked).isGreaterThanOrEqualTo(1);
    refreshTokenRepository
        .findAll()
        .forEach(t -> assertThat(t.isRevoked()).as("token revoked").isTrue());
  }
}
