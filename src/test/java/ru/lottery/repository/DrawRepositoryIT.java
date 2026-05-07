package ru.lottery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import ru.lottery.model.Draw;
import ru.lottery.model.LotteryType;
import ru.lottery.model.enums.DrawStatus;
import ru.lottery.support.AbstractIntegrationTest;

class DrawRepositoryIT extends AbstractIntegrationTest {

  @Inject DrawRepository drawRepository;

  private Draw seedDraw(String name, DrawStatus status, LocalDateTime drawDate, UUID typeId) {
    Draw d = new Draw();
    d.setName(name);
    d.setStatus(status);
    d.setDrawDate(drawDate);
    LotteryType type = new LotteryType();
    type.setId(typeId);
    d.setLotteryType(type);
    return drawRepository.save(d);
  }

  @Test
  void findByStatusEagerLoadsLotteryType() {
    seedDraw("D1", DrawStatus.ACTIVE, LocalDateTime.now().plusDays(1), CLASSIC_TYPE_ID);

    var draws = drawRepository.findByStatus(DrawStatus.ACTIVE);
    assertThat(draws).hasSize(1);
    assertThat(draws.get(0).getLotteryType()).isNotNull();
    assertThat(draws.get(0).getLotteryType().getName()).isEqualTo("Classic 6/49");
  }

  @Test
  void queryByIdEagerLoadsLotteryType() {
    Draw d = seedDraw("D2", DrawStatus.CREATED, LocalDateTime.now().plusDays(1), CLASSIC_TYPE_ID);
    var found = drawRepository.queryById(d.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getLotteryType().getPicks()).isEqualTo(6);
  }

  @Test
  void findByStatusAndDrawDateBetween() {
    LocalDateTime base = LocalDateTime.of(2026, 6, 15, 12, 0);
    seedDraw("InRange", DrawStatus.FINISHED, base, CLASSIC_TYPE_ID);
    seedDraw("OutOfRange", DrawStatus.FINISHED, base.minusYears(2), CLASSIC_TYPE_ID);

    var rows =
        drawRepository.findByStatusAndDrawDateBetween(
            DrawStatus.FINISHED, base.minusDays(1), base.plusDays(1));
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).getName()).isEqualTo("InRange");
  }

  @Test
  void findByStatusAndLotteryTypeIdHandlesBothCombos() {
    LotteryType extra = new LotteryType();
    extra.setName("Mini 5/36");
    extra.setPoolMin(1);
    extra.setPoolMax(36);
    extra.setPicks(5);
    UUID extraId = lotteryTypeRepository.save(extra).getId();

    LocalDateTime base = LocalDateTime.of(2026, 7, 1, 12, 0);
    seedDraw("Classic", DrawStatus.FINISHED, base, CLASSIC_TYPE_ID);
    seedDraw("Mini", DrawStatus.FINISHED, base, extraId);

    assertThat(drawRepository.findByStatusAndLotteryTypeId(DrawStatus.FINISHED, extraId))
        .hasSize(1);
    assertThat(
            drawRepository.findByStatusAndLotteryTypeIdAndDrawDateBetween(
                DrawStatus.FINISHED, extraId, base.minusDays(1), base.plusDays(1)))
        .hasSize(1);
  }
}
