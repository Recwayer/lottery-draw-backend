package ru.lottery.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class BaseEntity {
  @Id private UUID id;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
