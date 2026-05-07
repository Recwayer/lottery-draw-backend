package ru.lottery.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
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
import ru.lottery.web.dto.NotificationPayload;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.sse.SseClient;
import io.micronaut.http.sse.Event;
import io.micronaut.security.token.render.BearerAccessRefreshToken;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class NotificationControllerIT extends AbstractWebIntegrationTest {

  @Test
  void streamReceivesEventsAfterRunDraw() {
    DrawService drawService = bean(DrawService.class);
    TicketService ticketService = bean(TicketService.class);
    User user = seedUser("n@b", "password");
    BearerAccessRefreshToken token = login("n@b", "password");
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());
    ticketService.buy(user.getEmail(), draw.getId());

    SseClient sseClient = ctx.createBean(SseClient.class, embeddedServer.getURL());
    Flux<Event<NotificationPayload>> stream =
        Flux.from(
            sseClient.eventStream(
                HttpRequest.GET("/notifications/stream")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .bearerAuth(token.getAccessToken()),
                NotificationPayload.class));

    StepVerifier.create(stream)
        .then(
            () -> {
              try {
                Thread.sleep(300);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
              drawService.runDraw(draw.getId());
            })
        .assertNext(event -> assertThat(event.getData().drawId()).isEqualTo(draw.getId()))
        .thenCancel()
        .verify(Duration.ofSeconds(5));
  }

  @Test
  void recentReturnsNotificationSentEvents() {
    UserEventRecorder userEventRecorder = bean(UserEventRecorder.class);
    User user = seedUser("r@b", "password");
    BearerAccessRefreshToken token = login("r@b", "password");
    NotificationPayload payload =
        new NotificationPayload(null, null, "x", "y", "1", "1", null, "ok");
    userEventRecorder.record(user, UserEventType.NOTIFICATION_SENT, payload);

    List<Map<String, Object>> response =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.GET("/notifications/recent").bearerAuth(token.getAccessToken()),
                Argument.listOf(Argument.mapOf(String.class, Object.class)));
    assertThat(response).isNotEmpty();
    assertThat(response)
        .extracting(m -> (Object) m.get("type"))
        .contains(UserEventType.NOTIFICATION_SENT.name());
  }
}
