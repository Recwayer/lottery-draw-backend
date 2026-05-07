package ru.lottery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.lottery.model.Draw;
import ru.lottery.model.DrawResult;
import ru.lottery.model.LotteryType;
import ru.lottery.model.enums.DrawStatus;
import ru.lottery.model.enums.TicketStatus;
import ru.lottery.repository.DrawRepository;
import ru.lottery.repository.DrawResultRepository;
import ru.lottery.repository.TicketRepository;
import ru.lottery.web.dto.DrawReportRow;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  @Mock DrawRepository drawRepository;
  @Mock DrawResultRepository drawResultRepository;
  @Mock TicketRepository ticketRepository;
  @InjectMocks ReportService service;

  private static Draw draw(UUID id, LotteryType type) {
    Draw d = new Draw();
    d.setId(id);
    d.setName("Big Draw");
    d.setStatus(DrawStatus.FINISHED);
    d.setDrawDate(LocalDateTime.of(2026, 4, 25, 12, 0));
    d.setLotteryType(type);
    return d;
  }

  private static LotteryType type(String name) {
    LotteryType t = new LotteryType();
    t.setName(name);
    return t;
  }

  @Test
  void noFilters() {
    Draw d = draw(UUID.randomUUID(), type("Classic"));
    when(drawRepository.findByStatus(DrawStatus.FINISHED)).thenReturn(List.of(d));
    when(drawResultRepository.findByDrawId(d.getId())).thenReturn(Optional.empty());
    when(ticketRepository.countByDrawId(d.getId())).thenReturn(5L);
    when(ticketRepository.countByDrawIdAndStatus(d.getId(), TicketStatus.WIN)).thenReturn(1L);

    List<DrawReportRow> rows =
        service.finishedDraws(Optional.empty(), Optional.empty(), Optional.empty());

    assertThat(rows).hasSize(1);
    DrawReportRow row = rows.get(0);
    assertThat(row.lotteryType()).isEqualTo("Classic");
    assertThat(row.totalTickets()).isEqualTo(5);
    assertThat(row.winners()).isEqualTo(1);
    assertThat(row.winningNumbers()).isNull();
    verify(drawRepository, never()).findByStatusAndLotteryTypeId(any(), any());
  }

  @Test
  void rangeOnly() {
    LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
    LocalDateTime to = LocalDateTime.of(2026, 12, 31, 23, 59);
    Draw d = draw(UUID.randomUUID(), null);
    when(drawRepository.findByStatusAndDrawDateBetween(
            eq(DrawStatus.FINISHED), eq(from), any(LocalDateTime.class)))
        .thenReturn(List.of(d));
    DrawResult result = new DrawResult();
    result.setWinningNumbers("1,2,3");
    when(drawResultRepository.findByDrawId(d.getId())).thenReturn(Optional.of(result));

    List<DrawReportRow> rows =
        service.finishedDraws(Optional.of(from), Optional.of(to), Optional.empty());

    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).winningNumbers()).isEqualTo("1,2,3");
    assertThat(rows.get(0).lotteryType()).isNull();
  }

  @Test
  void typeOnly() {
    UUID typeId = UUID.randomUUID();
    Draw d = draw(UUID.randomUUID(), type("Mini"));
    when(drawRepository.findByStatusAndLotteryTypeId(DrawStatus.FINISHED, typeId))
        .thenReturn(List.of(d));

    service.finishedDraws(Optional.empty(), Optional.empty(), Optional.of(typeId));

    verify(drawRepository).findByStatusAndLotteryTypeId(DrawStatus.FINISHED, typeId);
  }

  @Test
  void typeAndRange() {
    UUID typeId = UUID.randomUUID();
    LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
    Draw d = draw(UUID.randomUUID(), type("Mini"));
    when(drawRepository.findByStatusAndLotteryTypeIdAndDrawDateBetween(
            eq(DrawStatus.FINISHED), eq(typeId), any(), any()))
        .thenReturn(List.of(d));

    service.finishedDraws(Optional.of(from), Optional.empty(), Optional.of(typeId));

    verify(drawRepository)
        .findByStatusAndLotteryTypeIdAndDrawDateBetween(
            eq(DrawStatus.FINISHED), eq(typeId), eq(from), any());
  }
}
