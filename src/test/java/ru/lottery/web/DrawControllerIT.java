package ru.lottery.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ru.lottery.model.enums.DrawStatus;
import ru.lottery.support.AbstractWebIntegrationTest;
import ru.lottery.web.dto.CreateDrawRequest;
import ru.lottery.web.dto.DrawResponse;
import ru.lottery.web.dto.DrawRunResponse;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.token.render.BearerAccessRefreshToken;

class DrawControllerIT extends AbstractWebIntegrationTest {

  @Test
  void adminCreatesAndStartsDraw() {
    seedAdmin("admin@b", "password");
    BearerAccessRefreshToken admin = login("admin@b", "password");

    CreateDrawRequest body =
        new CreateDrawRequest("D1", LocalDateTime.now().plusHours(2), CLASSIC_TYPE_ID);
    DrawResponse created =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/admin/draws", body).bearerAuth(admin.getAccessToken()),
                DrawResponse.class);
    assertThat(created.id()).isNotNull();
    assertThat(created.status()).isEqualTo(DrawStatus.CREATED);

    DrawResponse started =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/admin/draws/" + created.id() + "/start", null)
                    .bearerAuth(admin.getAccessToken()),
                DrawResponse.class);
    assertThat(started.status()).isEqualTo(DrawStatus.ACTIVE);

    DrawRunResponse run =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/admin/draws/" + created.id() + "/run", null)
                    .bearerAuth(admin.getAccessToken()),
                DrawRunResponse.class);
    assertThat(run.drawId()).isEqualTo(created.id());
    assertThat(run.winningNumbers()).isNotBlank();
  }

  @Test
  void adminCanCancel() {
    seedAdmin("admin2@b", "password");
    BearerAccessRefreshToken admin = login("admin2@b", "password");

    DrawResponse created =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.POST(
                        "/admin/draws",
                        new CreateDrawRequest(
                            "C", LocalDateTime.now().plusHours(2), CLASSIC_TYPE_ID))
                    .bearerAuth(admin.getAccessToken()),
                DrawResponse.class);
    DrawResponse cancelled =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/admin/draws/" + created.id() + "/cancel", null)
                    .bearerAuth(admin.getAccessToken()),
                DrawResponse.class);
    assertThat(cancelled.status()).isEqualTo(DrawStatus.CANCELED);
  }

  @Test
  void listActiveVisibleToUser() {
    seedAdmin("admin3@b", "password");
    seedUser("user@b", "password");
    BearerAccessRefreshToken admin = login("admin3@b", "password");
    BearerAccessRefreshToken user = login("user@b", "password");

    DrawResponse created =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.POST(
                        "/admin/draws",
                        new CreateDrawRequest(
                            "List", LocalDateTime.now().plusHours(2), CLASSIC_TYPE_ID))
                    .bearerAuth(admin.getAccessToken()),
                DrawResponse.class);
    httpClient
        .toBlocking()
        .exchange(
            HttpRequest.POST("/admin/draws/" + created.id() + "/start", null)
                .bearerAuth(admin.getAccessToken()));

    List<DrawResponse> active =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.GET("/draws/active").bearerAuth(user.getAccessToken()),
                Argument.listOf(DrawResponse.class));
    assertThat(active).extracting(DrawResponse::id).contains(created.id());
  }

  @Test
  void userCannotCallAdminEndpoint() {
    seedUser("u@b", "password");
    BearerAccessRefreshToken user = login("u@b", "password");

    assertThatThrownBy(
            () ->
                httpClient
                    .toBlocking()
                    .exchange(
                        HttpRequest.POST(
                                "/admin/draws",
                                new CreateDrawRequest(
                                    "Bad", LocalDateTime.now().plusHours(2), CLASSIC_TYPE_ID))
                            .bearerAuth(user.getAccessToken())))
        .isInstanceOf(HttpClientResponseException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void anonymousIsUnauthorized() {
    assertThatThrownBy(() -> httpClient.toBlocking().exchange(HttpRequest.GET("/draws/active")))
        .isInstanceOf(HttpClientResponseException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void getMissingDrawNotFound() {
    seedAdmin("admin4@b", "password");
    BearerAccessRefreshToken admin = login("admin4@b", "password");
    assertThatThrownBy(
            () ->
                httpClient
                    .toBlocking()
                    .exchange(
                        HttpRequest.POST("/admin/draws/" + UUID.randomUUID() + "/start", null)
                            .bearerAuth(admin.getAccessToken())))
        .isInstanceOf(HttpClientResponseException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.NOT_FOUND);
  }
}
