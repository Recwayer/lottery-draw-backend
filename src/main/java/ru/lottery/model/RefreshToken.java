package ru.lottery.model;

import java.time.LocalDateTime;

import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.Relation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedEntity("refresh_token")
public class RefreshToken extends BaseEntity {

  @Relation(value = Relation.Kind.MANY_TO_ONE)
  private User user;

  private String token;

  private boolean revoked;

  @MappedProperty("expires_at")
  private LocalDateTime expiresAt;
}
