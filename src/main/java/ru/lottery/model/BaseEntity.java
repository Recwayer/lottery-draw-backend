package ru.lottery.model;

import java.time.LocalDateTime;
import java.util.UUID;

import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity {

  @Id @AutoPopulated private UUID id;

  @DateCreated
  @MappedProperty("created_at")
  private LocalDateTime createdAt;

  @DateUpdated
  @MappedProperty("updated_at")
  private LocalDateTime updatedAt;
}
