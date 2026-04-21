package ru.lottery.model;

import ru.lottery.model.enums.TicketStatus;

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
@MappedEntity("ticket")
public class Ticket extends BaseEntity {

  @Relation(value = Relation.Kind.MANY_TO_ONE)
  private User user;

  @Relation(value = Relation.Kind.MANY_TO_ONE)
  private Draw draw;

  private String numbers;

  @TypeDef(type = DataType.STRING)
  private TicketStatus status;
}
