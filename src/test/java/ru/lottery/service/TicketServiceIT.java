package ru.lottery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import ru.lottery.model.Draw;
import ru.lottery.model.Ticket;
import ru.lottery.model.User;
import ru.lottery.model.enums.TicketStatus;
import ru.lottery.model.enums.UserEventType;
import ru.lottery.repository.UserEventRepository;
import ru.lottery.support.AbstractIntegrationTest;

import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

class TicketServiceIT extends AbstractIntegrationTest {

  @Inject DrawService drawService;
  @Inject TicketService ticketService;
  @Inject UserEventRepository userEventRepository;

  @Test
  void buyOnNonActiveThrowsConflict() {
    seedUser("a@b", "pw");
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    assertThatThrownBy(() -> ticketService.buy("a@b", draw.getId()))
        .isInstanceOf(HttpStatusException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void buyHappyPathRecordsBuyTicketEvent() {
    User user = seedUser("happy@b", "pw");
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());

    Ticket t = ticketService.buy("happy@b", draw.getId());
    assertThat(t.getStatus()).isEqualTo(TicketStatus.PAID);
    assertThat(t.getNumbers().split(",")).hasSize(6);

    var events =
        userEventRepository.findByUserIdAndTypeOrderByCreatedAtDesc(
            user.getId(), UserEventType.BUY_TICKET, Pageable.from(0, 10));
    assertThat(events).hasSize(1);
  }

  @Test
  void getTicketForeignUserGetsForbidden() {
    seedUser("owner@b", "pw");
    seedUser("intruder@b", "pw");
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());
    Ticket t = ticketService.buy("owner@b", draw.getId());

    assertThatThrownBy(() -> ticketService.get(t.getId(), "intruder@b"))
        .isInstanceOf(HttpStatusException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void getMissingTicket404() {
    seedUser("u@b", "pw");
    assertThatThrownBy(() -> ticketService.get(UUID.randomUUID(), "u@b"))
        .isInstanceOf(HttpStatusException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void listForUserReturnsOnlyOwnerTickets() {
    seedUser("o1@b", "pw");
    seedUser("o2@b", "pw");
    Draw draw = drawService.create("D", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());
    ticketService.buy("o1@b", draw.getId());
    ticketService.buy("o2@b", draw.getId());

    List<Ticket> tickets = ticketService.listForUser("o1@b");
    assertThat(tickets).hasSize(1);
    assertThat(tickets.get(0).getUser().getEmail()).isEqualTo("o1@b");
  }
}
