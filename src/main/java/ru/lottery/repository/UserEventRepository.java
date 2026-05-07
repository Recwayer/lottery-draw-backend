package ru.lottery.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import ru.lottery.model.UserEvent;
import ru.lottery.model.enums.UserEventType;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface UserEventRepository extends CrudRepository<UserEvent, UUID> {

    @Join(value = "user", type = Join.Type.FETCH)
    Page<UserEvent> findByUserId(UUID userId, Pageable pageable);

    @Join(value = "user", type = Join.Type.FETCH)
    Page<UserEvent> findByUserIdAndTypeIn(
            UUID userId, Collection<UserEventType> types, Pageable pageable);

    @Join(value = "user", type = Join.Type.FETCH)
    List<UserEvent> findByUserIdAndTypeOrderByCreatedAtDesc(
            UUID userId, UserEventType type, Pageable pageable);
}