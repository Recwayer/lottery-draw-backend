package ru.lottery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import ru.lottery.model.Draw;
import ru.lottery.model.DrawResult;
import ru.lottery.model.LotteryType;
import ru.lottery.model.enums.DrawStatus;
import ru.lottery.support.AbstractIntegrationTest;

class DrawResultRepositoryIT extends AbstractIntegrationTest {

  @Inject DrawRepository drawRepository;
  @Inject DrawResultRepository drawResultRepository;

  @Test
  void findByDrawIdReturnsResult() {
    Draw d = new Draw();
    d.setName("D");
    d.setStatus(DrawStatus.FINISHED);
    d.setDrawDate(LocalDateTime.now().plusDays(1));
    LotteryType type = new LotteryType();
    type.setId(CLASSIC_TYPE_ID);
    d.setLotteryType(type);
    Draw saved = drawRepository.save(d);

    DrawResult result = new DrawResult();
    result.setDraw(saved);
    result.setWinningNumbers("1,2,3,4,5,6");
    drawResultRepository.save(result);

    assertThat(drawResultRepository.findByDrawId(saved.getId())).isPresent();
  }
}
