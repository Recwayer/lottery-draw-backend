package ru.lottery.mapper;

import java.util.Arrays;
import java.util.List;

import ru.lottery.dto.ticket.TicketCabinetResponse;
import ru.lottery.model.Ticket;
import ru.lottery.util.JsonUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class TicketMapper {

  public TicketCabinetResponse toCabinetResponse(Ticket ticket) {
    return new TicketCabinetResponse(
        ticket.getId(),
        ticket.getDraw().getId(),
        parseNumbers(ticket.getNumbers()),
        ticket.getStatus(),
        ticket.getCreatedAt(),
        ticket.getUpdatedAt());
  }

  private List<Integer> parseNumbers(String numbers) {
    if (numbers == null || numbers.isBlank()) {
      return List.of();
    }

    String trimmed = numbers.trim();
    if (trimmed.startsWith("[")) {
      try {
        return JsonUtil.getMapper().readValue(trimmed, new TypeReference<>() {});
      } catch (Exception e) {
        log.warn("Ticket numbers are not a valid JSON array");
        return List.of();
      }
    }

    try {
      return Arrays.stream(trimmed.split(","))
          .map(String::trim)
          .filter(value -> !value.isBlank())
          .map(Integer::parseInt)
          .toList();
    } catch (NumberFormatException e) {
      log.warn("Ticket numbers are not valid comma-separated integers");
      return List.of();
    }
  }
}
