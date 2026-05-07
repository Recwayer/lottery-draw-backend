package ru.lottery.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;

import ru.lottery.aop.UserEventAspect;
import ru.lottery.model.Draw;
import ru.lottery.model.Ticket;
import ru.lottery.model.User;
import ru.lottery.model.enums.DrawStatus;
import ru.lottery.model.enums.TicketStatus;
import ru.lottery.repository.TicketRepository;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class TicketService {

  private final TicketRepository ticketRepository;
  private final DrawService drawService;
  private final UserService userService;
  private final LotteryDrawingStrategy drawingStrategy;
  private final UserEventAspect userEventAspect;

  @Transactional
  public Ticket buy(String userEmail, UUID drawId) {
    User user = userService.getByEmail(userEmail);
    Draw draw = drawService.getById(drawId);

    if (draw.getStatus() != DrawStatus.ACTIVE) {
      throw new HttpStatusException(
          HttpStatus.CONFLICT,
          "Tickets can be bought only for ACTIVE draws, current=" + draw.getStatus());
    }

    Ticket ticket = new Ticket();
    ticket.setUser(user);
    ticket.setDraw(draw);
    ticket.setNumbers(drawingStrategy.generate(draw.getLotteryType()));
    ticket.setStatus(TicketStatus.PAID);
    Ticket saved = ticketRepository.save(ticket);

    Map<String, Object> payload = new HashMap<>();
    payload.put("drawId", draw.getId());
    payload.put("ticketId", saved.getId());
    payload.put("numbers", saved.getNumbers());
    payload.put(
        "lotteryType", draw.getLotteryType() != null ? draw.getLotteryType().getName() : null);
    userEventAspect.buyTicket(user, payload);

    return saved;
  }

  public List<Ticket> listForUser(String userEmail) {
    return listForUser(userEmail, null, null);
  }

  public List<Ticket> listForUser(
      String userEmail, @Nullable UUID drawId, @Nullable TicketStatus status) {
    if (drawId != null && status != null) {
      return ticketRepository.findByUserEmailAndDrawIdAndStatusOrderByCreatedAtDesc(
          userEmail, drawId, status);
    }
    if (drawId != null) {
      return ticketRepository.findByUserEmailAndDrawIdOrderByCreatedAtDesc(userEmail, drawId);
    }
    if (status != null) {
      return ticketRepository.findByUserEmailAndStatusOrderByCreatedAtDesc(userEmail, status);
    }
    return ticketRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
  }

  public Ticket get(UUID ticketId, String userEmail) {
    Ticket ticket =
        ticketRepository
            .queryById(ticketId)
            .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    if (ticket.getUser() == null || !userEmail.equals(ticket.getUser().getEmail())) {
      throw new HttpStatusException(HttpStatus.FORBIDDEN, "Ticket does not belong to you");
    }
    return ticket;
  }
}
