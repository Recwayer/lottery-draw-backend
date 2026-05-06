package ru.lottery.web.dto;

import java.util.UUID;

import ru.lottery.model.Ticket;
import ru.lottery.model.enums.TicketStatus;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record TicketResponse(UUID id, UUID drawId, String numbers, TicketStatus status) {

  public static TicketResponse from(Ticket ticket) {
    return new TicketResponse(
        ticket.getId(), ticket.getDraw().getId(), ticket.getNumbers(), ticket.getStatus());
  }
}