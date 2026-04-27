package ru.lottery.web;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ru.lottery.security.AuthFilter;
import ru.lottery.service.TicketCabinetService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserTicketsServlet extends AbstractApiServlet {
  private final TicketCabinetService ticketCabinetService;

  public UserTicketsServlet(TicketCabinetService ticketCabinetService) {
    this.ticketCabinetService = ticketCabinetService;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    try {
      writeJson(
          response,
          HttpServletResponse.SC_OK,
          ticketCabinetService.getTickets(AuthFilter.getAuthenticatedUser(request)));
    } catch (Exception e) {
      writeError(response, e, log);
    }
  }
}
