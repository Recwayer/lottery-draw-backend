package ru.lottery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import ru.lottery.model.LotteryType;

class LotteryDrawingStrategyTest {

  private final LotteryDrawingStrategy strategy = new LotteryDrawingStrategy();

  private static LotteryType type(int min, int max, int picks) {
    LotteryType type = new LotteryType();
    type.setName("test");
    type.setPoolMin(min);
    type.setPoolMax(max);
    type.setPicks(picks);
    return type;
  }

  @Test
  void generateRequiresType() {
    assertThatThrownBy(() -> strategy.generate(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Lottery type");
  }

  @Test
  void generateRejectsInvalidPool() {
    assertThatThrownBy(() -> strategy.generate(type(10, 5, 3)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("pool_min");
  }

  @Test
  void generateRejectsInvalidPicks() {
    assertThatThrownBy(() -> strategy.generate(type(1, 5, 0)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("picks");
    assertThatThrownBy(() -> strategy.generate(type(1, 5, 6)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("picks");
  }

  @RepeatedTest(20)
  void generateProducesUniqueValuesInRange() {
    LotteryType t = type(1, 49, 6);
    String csv = strategy.generate(t);
    Set<Integer> nums =
        new TreeSet<>(Arrays.stream(csv.split(",")).map(Integer::parseInt).toList());
    assertThat(nums).hasSize(6);
    assertThat(nums).allSatisfy(n -> assertThat(n).isBetween(1, 49));
  }

  @Test
  void generateAtFullRangeReturnsAllValues() {
    LotteryType t = type(1, 5, 5);
    String csv = strategy.generate(t);
    assertThat(Arrays.stream(csv.split(",")).map(Integer::parseInt).toList())
        .containsExactlyInAnyOrder(1, 2, 3, 4, 5);
  }

  @Test
  void isWinnerReturnsFalseOnNulls() {
    assertThat(strategy.isWinner(null, "1,2,3")).isFalse();
    assertThat(strategy.isWinner("1,2,3", null)).isFalse();
  }

  @Test
  void isWinnerCompareSets() {
    assertThat(strategy.isWinner("1,2,3", "3,2,1")).isTrue();
    assertThat(strategy.isWinner(" 1, 2, 3 ", "3,2,1")).isTrue();
    assertThat(strategy.isWinner("1,2,3,", "1,2,3")).isTrue();
    assertThat(strategy.isWinner("1,2,4", "3,2,1")).isFalse();
  }
}
