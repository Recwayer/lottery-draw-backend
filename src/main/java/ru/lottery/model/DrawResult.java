package ru.lottery.model;

import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.Relation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedEntity("draw_result")
public class DrawResult extends BaseEntity {

  @Relation(value = Relation.Kind.ONE_TO_ONE)
  private Draw draw;

  @MappedProperty("winning_numbers")
  private String winningNumbers;
}
