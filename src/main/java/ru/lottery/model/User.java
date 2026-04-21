package ru.lottery.model;

import ru.lottery.model.enums.Role;

import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedEntity("user")
public class User extends BaseEntity {

  private String email;

  @MappedProperty("password_hash")
  private String passwordHash;

  @TypeDef(type = DataType.STRING)
  private Role role;
}
