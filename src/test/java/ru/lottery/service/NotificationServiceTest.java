package ru.lottery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.lottery.aop.UserEventAspect;
import ru.lottery.model.enums.TicketStatus;
import ru.lottery.service.event.DrawCancelledEvent;
import ru.lottery.service.event.TicketEvaluatedEvent;
import ru.lottery.web.dto.NotificationPayload;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock UserEventAspect userEventAspect;

  NotificationService service;

  @BeforeEach
  void setUp() {
    service = new NotificationService(userEventAspect);
  }

  @Test
  void streamReturnsEmptyForNullEmail() {
    StepVerifier.create(service.stream(null)).expectComplete().verify(Duration.ofSeconds(1));
  }

  @Test
  void winEventIsBroadcastedAndRecorded() {
    Flux<NotificationPayload> flux = service.stream("a@b");

    UUID drawId = UUID.randomUUID();
    UUID ticketId = UUID.randomUUID();
    StepVerifier.create(flux)
        .then(
            () ->
                service.onTicketEvaluated(
                    new TicketEvaluatedEvent(
                        drawId,
                        "Big",
                        "Classic",
                        ticketId,
                        "a@b",
                        "1,2,3",
                        "1,2,3",
                        TicketStatus.WIN)))
        .assertNext(
            p -> {
              assertThat(p.status()).isEqualTo(TicketStatus.WIN);
              assertThat(p.message()).contains("Поздравляем");
              assertThat(p.drawId()).isEqualTo(drawId);
              assertThat(p.ticketId()).isEqualTo(ticketId);
            })
        .thenCancel()
        .verify(Duration.ofSeconds(2));

    verify(userEventAspect).notificationSent(eq("a@b"), any());
  }

  @Test
  void loseEventCarriesLoseMessage() {
    Flux<NotificationPayload> flux = service.stream("a@b");

    StepVerifier.create(flux)
        .then(
            () ->
                service.onTicketEvaluated(
                    new TicketEvaluatedEvent(
                        UUID.randomUUID(),
                        "n",
                        "Classic",
                        UUID.randomUUID(),
                        "a@b",
                        "1,2,3",
                        "4,5,6",
                        TicketStatus.LOSE)))
        .assertNext(p -> assertThat(p.message()).contains("не выиграл"))
        .thenCancel()
        .verify(Duration.ofSeconds(2));
  }

  @Test
  void ticketEventWithoutEmailIsIgnored() {
    service.onTicketEvaluated(
        new TicketEvaluatedEvent(
            UUID.randomUUID(),
            "n",
            "Classic",
            UUID.randomUUID(),
            null,
            "1,2,3",
            "4,5,6",
            TicketStatus.LOSE));
    verify(userEventAspect, never()).notificationSent(any(), any());
  }

  @Test
  void cancelledEventBroadcastsToAllOwners() {
    Flux<NotificationPayload> a = service.stream("a@b");
    Flux<NotificationPayload> b = service.stream("c@d");

    UUID drawId = UUID.randomUUID();
    DrawCancelledEvent event =
        new DrawCancelledEvent(drawId, "Big", List.of(), List.of("a@b", "c@d"));

    StepVerifier.create(a)
        .then(() -> service.onDrawCancelled(event))
        .assertNext(p -> assertThat(p.status()).isEqualTo(TicketStatus.CANCELED))
        .thenCancel()
        .verify(Duration.ofSeconds(2));

    StepVerifier.create(b)
        .then(() -> service.onDrawCancelled(event))
        .assertNext(p -> assertThat(p.message()).contains("отменён"))
        .thenCancel()
        .verify(Duration.ofSeconds(2));
  }

  @Test
  void cancelledEventWithoutEmailsIsIgnored() {
    service.onDrawCancelled(new DrawCancelledEvent(UUID.randomUUID(), "n", List.of(), null));
    verify(userEventAspect, never()).notificationSent(any(), any());
  }
}
