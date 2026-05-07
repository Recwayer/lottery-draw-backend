package ru.lottery.service;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Singleton;

import ru.lottery.model.LotteryType;
import ru.lottery.repository.LotteryTypeRepository;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class LotteryTypeService {

  private final LotteryTypeRepository lotteryTypeRepository;

  public LotteryType create(String name, int poolMin, int poolMax, int picks) {
    if (name == null || name.isBlank()) {
      throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Name is required");
    }
    if (poolMin >= poolMax) {
      throw new HttpStatusException(HttpStatus.BAD_REQUEST, "pool_min must be less than pool_max");
    }
    int range = poolMax - poolMin + 1;
    if (picks <= 0 || picks > range) {
      throw new HttpStatusException(
          HttpStatus.BAD_REQUEST, "picks must be in range (0, " + range + "]");
    }
    if (lotteryTypeRepository.existsByName(name)) {
      throw new HttpStatusException(
          HttpStatus.CONFLICT, "Lottery type with this name already exists");
    }

    LotteryType type = new LotteryType();
    type.setName(name);
    type.setPoolMin(poolMin);
    type.setPoolMax(poolMax);
    type.setPicks(picks);
    return lotteryTypeRepository.save(type);
  }

  public List<LotteryType> list() {
    return (List<LotteryType>) lotteryTypeRepository.findAll();
  }

  public LotteryType getById(UUID id) {
    if (id == null) {
      throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Lottery type id is required");
    }
    return lotteryTypeRepository
        .findById(id)
        .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Lottery type not found"));
  }
}
