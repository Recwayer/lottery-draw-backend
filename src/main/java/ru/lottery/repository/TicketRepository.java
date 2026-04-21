package ru.lottery.repository;

import org.hibernate.SessionFactory;

import ru.lottery.model.Ticket;

public class TicketRepository extends AbstractRepository<Ticket> {

  public TicketRepository(SessionFactory sessionFactory) {
    super(sessionFactory, Ticket.class);
  }
}
