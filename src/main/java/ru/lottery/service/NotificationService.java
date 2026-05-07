package ru.lottery.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.lottery.aop.UserEventAspect;
import ru.lottery.model.enums.TicketStatus;
import ru.lottery.service.event.DrawCancelledEvent;
import ru.lottery.service.event.TicketEvaluatedEvent;
import ru.lottery.web.dto.NotificationPayload;

import io.micronaut.runtime.event.annotation.EventListener;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Singleton
@RequiredArgsConstructor
public class NotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  private final UserEventAspect userEventAspect;
  private final Map<String, Sinks.Many<NotificationPayload>> sinks = new ConcurrentHashMap<>();

  public Flux<NotificationPayload> stream(String email) {
    if (email == null) {
      return Flux.empty();
    }
    Sinks.Many<NotificationPayload> sink =
        sinks.computeIfAbsent(email, k -> Sinks.many().multicast().directBestEffort());
    return sink.asFlux();
  }

  @EventListener
  public void onTicketEvaluated(TicketEvaluatedEvent event) {
    if (event.ownerEmail() == null) {
      return;
    }
    NotificationPayload payload =
        new NotificationPayload(
            event.drawId(),
            event.ticketId(),
            event.drawName(),
            event.lotteryType(),
            event.numbers(),
            event.winningNumbers(),
            event.status(),
            event.status() == TicketStatus.WIN
                ? "Поздравляем! Ваш билет выиграл."
                : "К сожалению, ваш билет не выиграл.");
    push(event.ownerEmail(), payload);
  }

  @EventListener
  public void onDrawCancelled(DrawCancelledEvent event) {
    if (event.ownerEmails() == null) {
      return;
    }
    for (String email : event.ownerEmails()) {
      NotificationPayload payload =
          new NotificationPayload(
              event.drawId(),
              null,
              event.drawName(),
              null,
              null,
              null,
              TicketStatus.CANCELED,
              "Тираж отменён, билеты будут возвращены.");
      push(email, payload);
    }
  }

  private void push(String email, NotificationPayload payload) {
    Optional.ofNullable(sinks.get(email))
        .ifPresent(
            s -> {
              Sinks.EmitResult result = s.tryEmitNext(payload);
              if (result.isFailure()) {
                LOG.debug("SSE emit failed for {}: {}", email, result);
              }
            });
    userEventAspect.notificationSent(email, payload);
  }
}
