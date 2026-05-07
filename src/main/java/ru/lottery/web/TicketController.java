package ru.lottery.web;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.Nullable;

import ru.lottery.model.Ticket;
import ru.lottery.model.enums.TicketStatus;
import ru.lottery.service.TicketService;
import ru.lottery.web.dto.TicketResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.RequiredArgsConstructor;

@Controller
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor
public class TicketController {

  private final TicketService ticketService;

  @Post("/draws/{id}/tickets")
  public HttpResponse<TicketResponse> buy(@PathVariable UUID id, Principal principal) {
    Ticket ticket = ticketService.buy(principal.getName(), id);
    return HttpResponse.created(TicketResponse.from(ticket));
  }

  @Get("/tickets/me")
  public List<TicketResponse> myTickets(
      Principal principal,
      @Nullable @QueryValue("drawId") UUID drawId,
      @Nullable @QueryValue("status") TicketStatus status) {
    return ticketService.listForUser(principal.getName(), drawId, status).stream()
        .map(TicketResponse::from)
        .toList();
  }

  @Get("/tickets/{id}")
  public TicketResponse get(@PathVariable UUID id, Principal principal) {
    return TicketResponse.from(ticketService.get(id, principal.getName()));
  }
}
