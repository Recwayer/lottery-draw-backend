package ru.lottery.service;

import java.util.List;

import ru.lottery.dto.ListResponse;
import ru.lottery.dto.ticket.TicketCabinetResponse;
import ru.lottery.mapper.TicketMapper;
import ru.lottery.repository.TicketRepository;
import ru.lottery.security.AuthenticatedUser;

public class TicketCabinetService {
  private final TicketRepository ticketRepository;

  public TicketCabinetService(TicketRepository ticketRepository) {
    this.ticketRepository = ticketRepository;
  }

  public ListResponse<TicketCabinetResponse> getTickets(AuthenticatedUser user) {
    List<TicketCabinetResponse> items =
        ticketRepository.findByUserId(user.userId()).stream()
            .map(TicketMapper::toCabinetResponse)
            .toList();
    return new ListResponse<>(items);
  }
}
