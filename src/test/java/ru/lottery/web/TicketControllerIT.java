package ru.lottery.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import ru.lottery.model.Draw;
import ru.lottery.model.User;
import ru.lottery.model.enums.TicketStatus;
import ru.lottery.service.DrawService;
import ru.lottery.service.TicketService;
import ru.lottery.support.AbstractWebIntegrationTest;
import ru.lottery.web.dto.TicketResponse;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.token.render.BearerAccessRefreshToken;

class TicketControllerIT extends AbstractWebIntegrationTest {

  @Test
  void buyHappyPath() {
    DrawService drawService = bean(DrawService.class);
    seedUser("u@b", "password");
    BearerAccessRefreshToken user = login("u@b", "password");
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());

    TicketResponse ticket =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/draws/" + draw.getId() + "/tickets", null)
                    .bearerAuth(user.getAccessToken()),
                TicketResponse.class);
    assertThat(ticket.status()).isEqualTo(TicketStatus.PAID);
    assertThat(ticket.numbers()).isNotBlank();
  }

  @Test
  void buyConflictForCreatedDraw() {
    DrawService drawService = bean(DrawService.class);
    seedUser("u@b", "password");
    BearerAccessRefreshToken user = login("u@b", "password");
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);

    assertThatThrownBy(
            () ->
                httpClient
                    .toBlocking()
                    .exchange(
                        HttpRequest.POST("/draws/" + draw.getId() + "/tickets", null)
                            .bearerAuth(user.getAccessToken())))
        .isInstanceOf(HttpClientResponseException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void listMyTickets() {
    DrawService drawService = bean(DrawService.class);
    TicketService ticketService = bean(TicketService.class);
    seedUser("u@b", "password");
    BearerAccessRefreshToken user = login("u@b", "password");
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());
    ticketService.buy("u@b", draw.getId());

    List<TicketResponse> tickets =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.GET("/tickets/me").bearerAuth(user.getAccessToken()),
                Argument.listOf(TicketResponse.class));
    assertThat(tickets).hasSize(1);
  }

  @Test
  void listMyTicketsFilteredByDrawIdAndStatus() {
    DrawService drawService = bean(DrawService.class);
    TicketService ticketService = bean(TicketService.class);
    seedUser("filter@b", "password");
    BearerAccessRefreshToken user = login("filter@b", "password");
    Draw d1 = drawService.create("D1", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    Draw d2 = drawService.create("D2", LocalDateTime.now().plusHours(2), CLASSIC_TYPE_ID);
    drawService.start(d1.getId());
    drawService.start(d2.getId());
    ticketService.buy("filter@b", d1.getId());
    ticketService.buy("filter@b", d2.getId());

    List<TicketResponse> onlyD1 =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.GET("/tickets/me?drawId=" + d1.getId())
                    .bearerAuth(user.getAccessToken()),
                Argument.listOf(TicketResponse.class));
    assertThat(onlyD1).hasSize(1);
    assertThat(onlyD1.getFirst().drawId()).isEqualTo(d1.getId());

    List<TicketResponse> paidOnly =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.GET("/tickets/me?status=PAID").bearerAuth(user.getAccessToken()),
                Argument.listOf(TicketResponse.class));
    assertThat(paidOnly).hasSize(2);

    List<TicketResponse> d1Paid =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.GET("/tickets/me?drawId=" + d1.getId() + "&status=PAID")
                    .bearerAuth(user.getAccessToken()),
                Argument.listOf(TicketResponse.class));
    assertThat(d1Paid).hasSize(1);
  }

  @Test
  void getTicketForeignUserForbidden() {
    DrawService drawService = bean(DrawService.class);
    TicketService ticketService = bean(TicketService.class);
    User owner = seedUser("o@b", "password");
    seedUser("intruder@b", "password");
    BearerAccessRefreshToken intruder = login("intruder@b", "password");
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());
    var ticket = ticketService.buy(owner.getEmail(), draw.getId());

    assertThatThrownBy(
            () ->
                httpClient
                    .toBlocking()
                    .exchange(
                        HttpRequest.GET("/tickets/" + ticket.getId())
                            .bearerAuth(intruder.getAccessToken())))
        .isInstanceOf(HttpClientResponseException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.FORBIDDEN);
  }
}
