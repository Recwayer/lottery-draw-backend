package ru.lottery.model;

import ru.lottery.model.enums.UserEventType;

import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedEntity("user_event")
public class UserEvent extends BaseEntity {

  @Relation(value = Relation.Kind.MANY_TO_ONE)
  private User user;

  @TypeDef(type = DataType.STRING)
  private UserEventType type;

  private String payload;
}
