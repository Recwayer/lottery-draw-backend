package ru.lottery.repository;

import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;

import ru.lottery.model.Ticket;
import ru.lottery.util.HibernateExecutor;

public class TicketRepository extends AbstractRepository<Ticket> {

  public TicketRepository(SessionFactory sessionFactory) {
    super(sessionFactory, Ticket.class);
  }

  public List<Ticket> findByUserId(UUID userId) {
    return HibernateExecutor.executeWithoutTransaction(
        sessionFactory,
        session ->
            session
                .createQuery(
                    """
                    select t
                    from Ticket t
                    join fetch t.draw
                    where t.user.id = :userId
                    order by t.createdAt desc
                    """,
                    Ticket.class)
                .setParameter("userId", userId)
                .getResultList());
  }
}
