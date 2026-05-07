package ru.lottery.web.dto;

import java.util.UUID;

import ru.lottery.model.LotteryType;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record LotteryTypeResponse(UUID id, String name, int poolMin, int poolMax, int picks) {

  public static LotteryTypeResponse from(LotteryType type) {
    if (type == null) {
      return null;
    }
    return new LotteryTypeResponse(
        type.getId(), type.getName(), type.getPoolMin(), type.getPoolMax(), type.getPicks());
  }
}
