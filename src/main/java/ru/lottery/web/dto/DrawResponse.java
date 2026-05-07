package ru.lottery.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import ru.lottery.model.Draw;
import ru.lottery.model.enums.DrawStatus;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record DrawResponse(
    UUID id,
    String name,
    DrawStatus status,
    LocalDateTime drawDate,
    LotteryTypeResponse lotteryType) {

  public static DrawResponse from(Draw draw) {
    return new DrawResponse(
        draw.getId(),
        draw.getName(),
        draw.getStatus(),
        draw.getDrawDate(),
        LotteryTypeResponse.from(draw.getLotteryType()));
  }
}
