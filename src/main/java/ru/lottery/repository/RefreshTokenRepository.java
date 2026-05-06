package ru.lottery.repository;

import java.util.Optional;
import java.util.UUID;

import ru.lottery.model.RefreshToken;
import ru.lottery.model.User;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, UUID> {

  @Join(value = "user", type = Join.Type.FETCH)
  Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

  @Query("UPDATE refresh_token SET revoked = true, updated_at = now() WHERE token = :token")
  void revokeByToken(String token);

  @Query(
      "UPDATE refresh_token SET revoked = true, updated_at = now() WHERE user_id = :userId AND revoked = false")
  void revokeAllByUser(UUID userId);

  default void revokeAllForUser(User user) {
    revokeAllByUser(user.getId());
  }
}
