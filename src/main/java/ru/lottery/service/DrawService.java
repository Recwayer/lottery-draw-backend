package ru.lottery.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.inject.Singleton;

import ru.lottery.aop.UserEventAspect;
import ru.lottery.model.Draw;
import ru.lottery.model.DrawResult;
import ru.lottery.model.LotteryType;
import ru.lottery.model.Ticket;
import ru.lottery.model.User;
import ru.lottery.model.enums.DrawStatus;
import ru.lottery.model.enums.TicketStatus;
import ru.lottery.repository.DrawRepository;
import ru.lottery.repository.DrawResultRepository;
import ru.lottery.repository.TicketRepository;
import ru.lottery.service.event.DrawCancelledEvent;
import ru.lottery.service.event.TicketEvaluatedEvent;
import ru.lottery.web.dto.DrawRunResponse;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class DrawService {

  private final DrawRepository drawRepository;
  private final DrawResultRepository drawResultRepository;
  private final TicketRepository ticketRepository;
  private final LotteryDrawingStrategy drawingStrategy;
  private final LotteryTypeService lotteryTypeService;
  private final UserEventAspect userEventAspect;
  private final ApplicationEventPublisher<Object> eventPublisher;

  public Draw create(String name, LocalDateTime drawDate, UUID lotteryTypeId) {
    LotteryType type = lotteryTypeService.getById(lotteryTypeId);
    Draw draw = new Draw();
    draw.setName(name);
    draw.setStatus(DrawStatus.CREATED);
    draw.setDrawDate(drawDate);
    draw.setLotteryType(type);
    return drawRepository.save(draw);
  }

  public List<Draw> listActive() {
    return drawRepository.findByStatus(DrawStatus.ACTIVE);
  }

  public Draw getById(UUID id) {
    return drawRepository
        .queryById(id)
        .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Draw not found"));
  }

  public Draw start(UUID id) {
    Draw draw = getById(id);
    if (draw.getStatus() != DrawStatus.CREATED) {
      throw new HttpStatusException(
          HttpStatus.CONFLICT, "Only CREATED draws can be started, current=" + draw.getStatus());
    }
    draw.setStatus(DrawStatus.ACTIVE);
    return drawRepository.update(draw);
  }

  @Transactional
  public DrawRunResponse runDraw(UUID id) {
    Draw draw = getById(id);
    if (draw.getStatus() != DrawStatus.ACTIVE) {
      throw new HttpStatusException(
          HttpStatus.CONFLICT, "Only ACTIVE draws can be run, current=" + draw.getStatus());
    }

    String winningNumbers = drawingStrategy.generate(draw.getLotteryType());

    DrawResult result = new DrawResult();
    result.setDraw(draw);
    result.setWinningNumbers(winningNumbers);
    drawResultRepository.save(result);

    List<Ticket> tickets = ticketRepository.findByDrawIdAndStatus(draw.getId(), TicketStatus.PAID);
    int winners = 0;
    String typeName = draw.getLotteryType() != null ? draw.getLotteryType().getName() : null;

    for (Ticket ticket : tickets) {
      boolean win = drawingStrategy.isWinner(ticket.getNumbers(), winningNumbers);
      TicketStatus newStatus = win ? TicketStatus.WIN : TicketStatus.LOSE;
      ticket.setStatus(newStatus);
      ticketRepository.update(ticket);
      if (win) {
        winners++;
      }

      String ownerEmail = ticket.getUser() != null ? ticket.getUser().getEmail() : null;

      Map<String, Object> payload = new HashMap<>();
      payload.put("drawId", draw.getId());
      payload.put("ticketId", ticket.getId());
      payload.put("numbers", ticket.getNumbers());
      payload.put("winningNumbers", winningNumbers);
      payload.put("status", newStatus);
      if (win) {
        userEventAspect.ticketWin(ticket.getUser(), payload);
      } else {
        userEventAspect.ticketLose(ticket.getUser(), payload);
      }

      eventPublisher.publishEvent(
          new TicketEvaluatedEvent(
              draw.getId(),
              draw.getName(),
              typeName,
              ticket.getId(),
              ownerEmail,
              ticket.getNumbers(),
              winningNumbers,
              newStatus));
    }

    draw.setStatus(DrawStatus.FINISHED);
    drawRepository.update(draw);

    return new DrawRunResponse(draw.getId(), winningNumbers, tickets.size(), winners);
  }

  @Transactional
  public Draw cancel(UUID id) {
    Draw draw = getById(id);
    if (draw.getStatus() != DrawStatus.CREATED && draw.getStatus() != DrawStatus.ACTIVE) {
      throw new HttpStatusException(
          HttpStatus.CONFLICT,
          "Only CREATED or ACTIVE draws can be canceled, current=" + draw.getStatus());
    }

    List<Ticket> created =
        ticketRepository.findByDrawIdAndStatus(draw.getId(), TicketStatus.CREATED);
    List<Ticket> paid = ticketRepository.findByDrawIdAndStatus(draw.getId(), TicketStatus.PAID);

    List<UUID> ticketIds = new ArrayList<>(created.size() + paid.size());
    List<String> ownerEmails = new ArrayList<>(created.size() + paid.size());

    Map<String, Object> drawPayload = Map.of("drawId", draw.getId(), "drawName", draw.getName());

    for (Ticket ticket : concat(created, paid)) {
      ticket.setStatus(TicketStatus.CANCELED);
      ticketRepository.update(ticket);
      ticketIds.add(ticket.getId());

      User owner = ticket.getUser();
      if (owner != null) {
        ownerEmails.add(owner.getEmail());
      }

      Map<String, Object> refundPayload = new HashMap<>();
      refundPayload.put("drawId", draw.getId());
      refundPayload.put("ticketId", ticket.getId());
      refundPayload.put("numbers", ticket.getNumbers());
      userEventAspect.ticketRefund(owner, refundPayload);
      userEventAspect.drawCanceled(owner, drawPayload);
    }

    draw.setStatus(DrawStatus.CANCELED);
    Draw updated = drawRepository.update(draw);

    eventPublisher.publishEvent(
        new DrawCancelledEvent(
            draw.getId(), draw.getName(), ticketIds, ownerEmails.stream().distinct().toList()));

    return updated;
  }

  private static List<Ticket> concat(List<Ticket> a, List<Ticket> b) {
    List<Ticket> all = new ArrayList<>(a.size() + b.size());
    all.addAll(a);
    all.addAll(b);
    return all;
  }
}