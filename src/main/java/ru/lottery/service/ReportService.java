package ru.lottery.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.inject.Singleton;

import ru.lottery.model.Draw;
import ru.lottery.model.enums.DrawStatus;
import ru.lottery.model.enums.TicketStatus;
import ru.lottery.repository.DrawRepository;
import ru.lottery.repository.DrawResultRepository;
import ru.lottery.repository.TicketRepository;
import ru.lottery.web.dto.DrawReportRow;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class ReportService {

  private final DrawRepository drawRepository;
  private final DrawResultRepository drawResultRepository;
  private final TicketRepository ticketRepository;

  public List<DrawReportRow> finishedDraws(
      Optional<LocalDateTime> from, Optional<LocalDateTime> to, Optional<UUID> typeId) {
    List<Draw> draws = fetch(from, to, typeId);
    return draws.stream().map(this::toRow).toList();
  }

  private List<Draw> fetch(
      Optional<LocalDateTime> from, Optional<LocalDateTime> to, Optional<UUID> typeId) {
    LocalDateTime fromDate = from.orElse(LocalDateTime.of(1970, 1, 1, 0, 0));
    LocalDateTime toDate = to.orElse(LocalDateTime.of(9999, 12, 31, 23, 59));
    boolean rangeProvided = from.isPresent() || to.isPresent();

    if (typeId.isPresent() && rangeProvided) {
      return drawRepository.findByStatusAndLotteryTypeIdAndDrawDateBetween(
          DrawStatus.FINISHED, typeId.get(), fromDate, toDate);
    }
    if (typeId.isPresent()) {
      return drawRepository.findByStatusAndLotteryTypeId(DrawStatus.FINISHED, typeId.get());
    }
    if (rangeProvided) {
      return drawRepository.findByStatusAndDrawDateBetween(DrawStatus.FINISHED, fromDate, toDate);
    }
    return drawRepository.findByStatus(DrawStatus.FINISHED);
  }

  private DrawReportRow toRow(Draw draw) {
    String winning =
        drawResultRepository
            .findByDrawId(draw.getId())
            .map(r -> r.getWinningNumbers())
            .orElse(null);
    long total = ticketRepository.countByDrawId(draw.getId());
    long winners = ticketRepository.countByDrawIdAndStatus(draw.getId(), TicketStatus.WIN);
    String typeName = draw.getLotteryType() != null ? draw.getLotteryType().getName() : null;
    return new DrawReportRow(
        draw.getId(), typeName, draw.getName(), draw.getDrawDate(), winning, total, winners);
  }
}
