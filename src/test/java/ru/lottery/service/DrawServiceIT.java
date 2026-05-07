package ru.lottery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.Test;

import ru.lottery.model.Draw;
import ru.lottery.model.Ticket;
import ru.lottery.model.User;
import ru.lottery.model.enums.DrawStatus;
import ru.lottery.model.enums.TicketStatus;
import ru.lottery.model.enums.UserEventType;
import ru.lottery.repository.TicketRepository;
import ru.lottery.repository.UserEventRepository;
import ru.lottery.service.event.DrawCancelledEvent;
import ru.lottery.service.event.TicketEvaluatedEvent;
import ru.lottery.support.AbstractIntegrationTest;
import ru.lottery.web.dto.DrawRunResponse;

import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.runtime.event.annotation.EventListener;

class DrawServiceIT extends AbstractIntegrationTest {

  @Inject DrawService drawService;
  @Inject TicketService ticketService;
  @Inject TicketRepository ticketRepository;
  @Inject UserEventRepository userEventRepository;
  @Inject EventCollector collector;

  @Singleton
  static class EventCollector {
    final List<TicketEvaluatedEvent> ticketEvents = new CopyOnWriteArrayList<>();
    final List<DrawCancelledEvent> drawCancelledEvents = new CopyOnWriteArrayList<>();

    @EventListener
    public void onTicket(TicketEvaluatedEvent e) {
      ticketEvents.add(e);
    }

    @EventListener
    public void onCancelled(DrawCancelledEvent e) {
      drawCancelledEvents.add(e);
    }

    void reset() {
      ticketEvents.clear();
      drawCancelledEvents.clear();
    }
  }

  @Test
  void fullFlowCreateStartBuyRun() {
    collector.reset();
    User user = seedUser("buyer@b", "pw");
    Draw draw = drawService.create("Big", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());
    Ticket ticket = ticketService.buy(user.getEmail(), draw.getId());
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PAID);

    DrawRunResponse response = drawService.runDraw(draw.getId());
    assertThat(response.total()).isEqualTo(1);
    assertThat(response.winningNumbers()).isNotBlank();

    Draw finished = drawService.getById(draw.getId());
    assertThat(finished.getStatus()).isEqualTo(DrawStatus.FINISHED);

    Ticket updated = ticketRepository.queryById(ticket.getId()).orElseThrow();
    assertThat(updated.getStatus()).isIn(TicketStatus.WIN, TicketStatus.LOSE);

    long buyEvents =
        userEventRepository
            .findByUserIdAndTypeOrderByCreatedAtDesc(
                user.getId(), UserEventType.BUY_TICKET, Pageable.from(0, 10))
            .size();
    assertThat(buyEvents).isEqualTo(1);

    assertThat(collector.ticketEvents).hasSize(1);
    assertThat(collector.ticketEvents.get(0).ownerEmail()).isEqualTo("buyer@b");
  }

  @Test
  void runRejectsNonActiveDraw() {
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    assertThatThrownBy(() -> drawService.runDraw(draw.getId()))
        .isInstanceOf(HttpStatusException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void startRejectsActiveDraw() {
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());
    assertThatThrownBy(() -> drawService.start(draw.getId()))
        .isInstanceOf(HttpStatusException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void getByIdMissingThrows() {
    assertThatThrownBy(() -> drawService.getById(UUID.randomUUID()))
        .isInstanceOf(HttpStatusException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void cancelRejectsFinishedDraw() {
    User user = seedUser("c@b", "pw");
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());
    ticketService.buy(user.getEmail(), draw.getId());
    drawService.runDraw(draw.getId());
    assertThatThrownBy(() -> drawService.cancel(draw.getId()))
        .isInstanceOf(HttpStatusException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void cancelMovesActiveDrawAndTicketsToCanceled() {
    collector.reset();
    User user = seedUser("cx@b", "pw");
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());
    Ticket ticket = ticketService.buy(user.getEmail(), draw.getId());

    Draw cancelled = drawService.cancel(draw.getId());
    assertThat(cancelled.getStatus()).isEqualTo(DrawStatus.CANCELED);

    Ticket reloaded = ticketRepository.queryById(ticket.getId()).orElseThrow();
    assertThat(reloaded.getStatus()).isEqualTo(TicketStatus.CANCELED);

    assertThat(collector.drawCancelledEvents).hasSize(1);
    assertThat(collector.drawCancelledEvents.get(0).ownerEmails()).contains("cx@b");

    long refundEvents =
        userEventRepository
            .findByUserIdAndTypeOrderByCreatedAtDesc(
                user.getId(), UserEventType.TICKET_REFUND, Pageable.from(0, 10))
            .size();
    assertThat(refundEvents).isEqualTo(1);
  }

  @Test
  void cancelFromCreatedAlsoWorks() {
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    Draw cancelled = drawService.cancel(draw.getId());
    assertThat(cancelled.getStatus()).isEqualTo(DrawStatus.CANCELED);
  }

  @Test
  void listActiveOnlyReturnsActive() {
    Draw a = drawService.create("A", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.create("B", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(a.getId());
    List<Draw> active = drawService.listActive();
    assertThat(active).extracting(Draw::getId).containsExactly(a.getId());
  }
}
