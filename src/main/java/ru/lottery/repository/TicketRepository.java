package ru.lottery.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ru.lottery.model.Ticket;
import ru.lottery.model.enums.TicketStatus;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface TicketRepository extends CrudRepository<Ticket, UUID> {

  @Join(value = "user", type = Join.Type.FETCH)
  @Join(value = "draw", type = Join.Type.FETCH)
  List<Ticket> findByUserEmailOrderByCreatedAtDesc(String email);

  @Join(value = "user", type = Join.Type.FETCH)
  @Join(value = "draw", type = Join.Type.FETCH)
  List<Ticket> findByUserEmailAndDrawIdOrderByCreatedAtDesc(String email, UUID drawId);

  @Join(value = "user", type = Join.Type.FETCH)
  @Join(value = "draw", type = Join.Type.FETCH)
  List<Ticket> findByUserEmailAndStatusOrderByCreatedAtDesc(String email, TicketStatus status);

  @Join(value = "user", type = Join.Type.FETCH)
  @Join(value = "draw", type = Join.Type.FETCH)
  List<Ticket> findByUserEmailAndDrawIdAndStatusOrderByCreatedAtDesc(
      String email, UUID drawId, TicketStatus status);

  @Join(value = "user", type = Join.Type.FETCH)
  @Join(value = "draw", type = Join.Type.FETCH)
  List<Ticket> findByDrawIdAndStatus(UUID drawId, TicketStatus status);

  @Join(value = "user", type = Join.Type.FETCH)
  @Join(value = "draw", type = Join.Type.FETCH)
  Optional<Ticket> queryById(UUID id);

  long countByDrawId(UUID drawId);

  long countByDrawIdAndStatus(UUID drawId, TicketStatus status);
}
