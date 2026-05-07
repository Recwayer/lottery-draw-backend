package ru.lottery.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import ru.lottery.support.AbstractWebIntegrationTest;
import ru.lottery.web.dto.CreateLotteryTypeRequest;
import ru.lottery.web.dto.LotteryTypeResponse;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.token.render.BearerAccessRefreshToken;

class LotteryTypeControllerIT extends AbstractWebIntegrationTest {

  @Test
  void adminCreatesLotteryType() {
    seedAdmin("admin@b", "password");
    BearerAccessRefreshToken admin = login("admin@b", "password");

    LotteryTypeResponse response =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.POST(
                        "/admin/lottery-types", new CreateLotteryTypeRequest("MegaTest", 1, 50, 5))
                    .bearerAuth(admin.getAccessToken()),
                LotteryTypeResponse.class);
    assertThat(response.name()).isEqualTo("MegaTest");
  }

  @Test
  void invalidPayloadReturns400() {
    seedAdmin("admin@b", "password");
    BearerAccessRefreshToken admin = login("admin@b", "password");

    assertThatThrownBy(
            () ->
                httpClient
                    .toBlocking()
                    .exchange(
                        HttpRequest.POST(
                                "/admin/lottery-types", new CreateLotteryTypeRequest("", 0, 1, 0))
                            .bearerAuth(admin.getAccessToken())))
        .isInstanceOf(HttpClientResponseException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void duplicateNameReturnsConflict() {
    seedAdmin("admin@b", "password");
    BearerAccessRefreshToken admin = login("admin@b", "password");

    httpClient
        .toBlocking()
        .exchange(
            HttpRequest.POST("/admin/lottery-types", new CreateLotteryTypeRequest("Dup", 1, 50, 5))
                .bearerAuth(admin.getAccessToken()));

    assertThatThrownBy(
            () ->
                httpClient
                    .toBlocking()
                    .exchange(
                        HttpRequest.POST(
                                "/admin/lottery-types",
                                new CreateLotteryTypeRequest("Dup", 1, 50, 5))
                            .bearerAuth(admin.getAccessToken())))
        .isInstanceOf(HttpClientResponseException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void userCanListTypes() {
    seedAdmin("admin@b", "password");
    BearerAccessRefreshToken admin = login("admin@b", "password");

    List<LotteryTypeResponse> types =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.GET("/admin/lottery-types").bearerAuth(admin.getAccessToken()),
                Argument.listOf(LotteryTypeResponse.class));
    assertThat(types).extracting(LotteryTypeResponse::name).contains("Classic 6/49");
  }
}
