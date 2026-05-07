package ru.lottery.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ru.lottery.model.Draw;
import ru.lottery.model.enums.DrawStatus;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface DrawRepository extends CrudRepository<Draw, UUID> {

  @Join(value = "lotteryType", type = Join.Type.FETCH)
  List<Draw> findByStatus(DrawStatus status);

  @Join(value = "lotteryType", type = Join.Type.FETCH)
  Optional<Draw> queryById(UUID id);

  @Join(value = "lotteryType", type = Join.Type.FETCH)
  List<Draw> findByStatusAndDrawDateBetween(
      DrawStatus status, LocalDateTime from, LocalDateTime to);

  @Join(value = "lotteryType", type = Join.Type.FETCH)
  List<Draw> findByStatusAndLotteryTypeId(DrawStatus status, UUID lotteryTypeId);

  @Join(value = "lotteryType", type = Join.Type.FETCH)
  List<Draw> findByStatusAndLotteryTypeIdAndDrawDateBetween(
      DrawStatus status, UUID lotteryTypeId, LocalDateTime from, LocalDateTime to);
}
