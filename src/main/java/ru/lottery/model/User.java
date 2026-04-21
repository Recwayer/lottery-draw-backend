package ru.lottery.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import ru.lottery.model.enums.Role;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;
}
