package ru.lottery.service;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import jakarta.inject.Singleton;

import ru.lottery.model.LotteryType;

@Singleton
public class LotteryDrawingStrategy {

  private final SecureRandom random = new SecureRandom();

  public String generate(LotteryType type) {
    if (type == null) {
      throw new IllegalArgumentException("Lottery type must be provided");
    }
    int poolMin = type.getPoolMin();
    int poolMax = type.getPoolMax();
    int picks = type.getPicks();
    if (poolMin >= poolMax) {
      throw new IllegalArgumentException("Lottery type pool_min must be less than pool_max");
    }
    int range = poolMax - poolMin + 1;
    if (picks <= 0 || picks > range) {
      throw new IllegalArgumentException(
          "Lottery type picks must be in (0, pool_max - pool_min + 1]");
    }

    Set<Integer> selected = new TreeSet<>();
    while (selected.size() < picks) {
      selected.add(random.nextInt(range) + poolMin);
    }
    return selected.stream().map(String::valueOf).collect(Collectors.joining(","));
  }

  public boolean isWinner(String ticketNumbers, String winningNumbers) {
    if (ticketNumbers == null || winningNumbers == null) {
      return false;
    }
    return parse(ticketNumbers).equals(parse(winningNumbers));
  }

  private Set<Integer> parse(String csv) {
    return Arrays.stream(csv.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(Integer::parseInt)
        .collect(Collectors.toCollection(HashSet::new));
  }
}