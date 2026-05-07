package ru.lottery.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ru.lottery.model.Draw;
import ru.lottery.model.User;
import ru.lottery.model.enums.UserEventType;
import ru.lottery.service.DrawService;
import ru.lottery.service.TicketService;
import ru.lottery.service.UserEventRecorder;
import ru.lottery.support.AbstractWebIntegrationTest;

import io.micronaut.http.HttpRequest;
import io.micronaut.security.token.render.BearerAccessRefreshToken;

class UserHistoryControllerIT extends AbstractWebIntegrationTest {

  @Test
  void historyReturnsRecordedEvents() {
    DrawService drawService = bean(DrawService.class);
    TicketService ticketService = bean(TicketService.class);
    User user = seedUser("h@b", "password");
    BearerAccessRefreshToken token = login("h@b", "password");
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());
    ticketService.buy(user.getEmail(), draw.getId());

    Map<?, ?> page =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.GET("/me/history?page=0&size=20").bearerAuth(token.getAccessToken()),
                Map.class);
    List<?> content = (List<?>) page.get("content");
    assertThat(content).isNotEmpty();
    assertThat(content)
        .extracting(o -> (Object) ((Map<?, ?>) o).get("type"))
        .contains(UserEventType.BUY_TICKET.name());
  }

  @Test
  void historyFilterByType() {
    UserEventRecorder userEventRecorder = bean(UserEventRecorder.class);
    User user = seedUser("h2@b", "password");
    BearerAccessRefreshToken token = login("h2@b", "password");
    userEventRecorder.record(user, UserEventType.LOGIN, Map.of("a", 1));
    userEventRecorder.record(user, UserEventType.LOGOUT, Map.of("b", 2));

    Map<?, ?> page =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.GET("/me/history?type=LOGIN").bearerAuth(token.getAccessToken()),
                Map.class);
    List<?> content = (List<?>) page.get("content");
    assertThat(content).isNotEmpty();
    assertThat(content)
        .allSatisfy(
            o -> assertThat(((Map<?, ?>) o).get("type")).isEqualTo(UserEventType.LOGIN.name()));
  }
}
