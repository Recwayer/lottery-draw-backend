package ru.lottery.repository;

import java.util.Optional;
import java.util.UUID;

import ru.lottery.model.DrawResult;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface DrawResultRepository extends CrudRepository<DrawResult, UUID> {

  Optional<DrawResult> findByDrawId(UUID drawId);
}
