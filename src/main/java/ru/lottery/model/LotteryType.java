package ru.lottery.model;

import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedEntity("lottery_type")
public class LotteryType extends BaseEntity {

  private String name;

  @MappedProperty("pool_min")
  private int poolMin;

  @MappedProperty("pool_max")
  private int poolMax;

  private int picks;
}
