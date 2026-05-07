package ru.lottery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import ru.lottery.model.Draw;
import ru.lottery.model.LotteryType;
import ru.lottery.model.Ticket;
import ru.lottery.model.User;
import ru.lottery.model.enums.DrawStatus;
import ru.lottery.model.enums.TicketStatus;
import ru.lottery.support.AbstractIntegrationTest;

class TicketRepositoryIT extends AbstractIntegrationTest {

  @Inject DrawRepository drawRepository;
  @Inject TicketRepository ticketRepository;

  private Draw seedDraw() {
    Draw d = new Draw();
    d.setName("D");
    d.setStatus(DrawStatus.ACTIVE);
    d.setDrawDate(LocalDateTime.now().plusDays(1));
    LotteryType type = new LotteryType();
    type.setId(CLASSIC_TYPE_ID);
    d.setLotteryType(type);
    return drawRepository.save(d);
  }

  private Ticket seedTicket(User u, Draw d, TicketStatus status) {
    Ticket t = new Ticket();
    t.setUser(u);
    t.setDraw(d);
    t.setNumbers("1,2,3,4,5,6");
    t.setStatus(status);
    return ticketRepository.save(t);
  }

  @Test
  void findByUserEmailAndDrawAndStatusOps() {
    User u1 = seedUser("u1@b", "pw");
    User u2 = seedUser("u2@b", "pw");
    Draw d = seedDraw();

    seedTicket(u1, d, TicketStatus.PAID);
    seedTicket(u1, d, TicketStatus.CREATED);
    seedTicket(u2, d, TicketStatus.PAID);

    assertThat(ticketRepository.findByUserEmailOrderByCreatedAtDesc("u1@b")).hasSize(2);
    assertThat(ticketRepository.findByDrawIdAndStatus(d.getId(), TicketStatus.PAID)).hasSize(2);
    assertThat(ticketRepository.countByDrawId(d.getId())).isEqualTo(3L);
    assertThat(ticketRepository.countByDrawIdAndStatus(d.getId(), TicketStatus.CREATED))
        .isEqualTo(1L);
  }

  @Test
  void queryByIdReturnsTicketWithRelations() {
    User u = seedUser("relations@b", "pw");
    Draw d = seedDraw();
    Ticket t = seedTicket(u, d, TicketStatus.PAID);
    UUID id = t.getId();

    var found = ticketRepository.queryById(id);
    assertThat(found).isPresent();
    assertThat(found.get().getUser().getEmail()).isEqualTo("relations@b");
    assertThat(found.get().getDraw().getId()).isEqualTo(d.getId());
  }
}
