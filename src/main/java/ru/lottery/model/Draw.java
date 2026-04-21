package ru.lottery.model;

import java.time.LocalDateTime;

import ru.lottery.model.enums.DrawStatus;

import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedEntity("draw")
public class Draw extends BaseEntity {

  private String name;

  @TypeDef(type = DataType.STRING)
  private DrawStatus status;

  @MappedProperty("draw_date")
  private LocalDateTime drawDate;

  @Relation(value = Relation.Kind.MANY_TO_ONE)
  @MappedProperty("lottery_type_id")
  private LotteryType lotteryType;
}
