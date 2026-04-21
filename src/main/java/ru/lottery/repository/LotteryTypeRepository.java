package ru.lottery.repository;

import java.util.UUID;

import ru.lottery.model.LotteryType;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface LotteryTypeRepository extends CrudRepository<LotteryType, UUID> {}
