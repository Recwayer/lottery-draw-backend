package ru.lottery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import ru.lottery.model.LotteryType;
import ru.lottery.support.AbstractIntegrationTest;

class LotteryTypeRepositoryIT extends AbstractIntegrationTest {

  @Test
  void seedTypeIsAvailable() {
    assertThat(lotteryTypeRepository.findById(CLASSIC_TYPE_ID)).isPresent();
    assertThat(lotteryTypeRepository.findByName("Classic 6/49")).isPresent();
    assertThat(lotteryTypeRepository.existsByName("Classic 6/49")).isTrue();
  }

  @Test
  void saveCustomType() {
    LotteryType type = new LotteryType();
    type.setName("Mini 5/36");
    type.setPoolMin(1);
    type.setPoolMax(36);
    type.setPicks(5);
    LotteryType saved = lotteryTypeRepository.save(type);

    assertThat(saved.getId()).isNotNull();
    assertThat(lotteryTypeRepository.existsByName("Mini 5/36")).isTrue();
  }
}
