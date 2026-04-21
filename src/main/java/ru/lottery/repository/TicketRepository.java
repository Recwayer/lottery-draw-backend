package ru.lottery.repository;

import java.util.UUID;

import ru.lottery.model.Ticket;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface TicketRepository extends CrudRepository<Ticket, UUID> {}
