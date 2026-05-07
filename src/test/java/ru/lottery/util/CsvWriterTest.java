package ru.lottery.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ru.lottery.web.dto.DrawReportRow;

class CsvWriterTest {

  @Test
  void emptyListRendersHeaderOnly() {
    String csv = CsvWriter.write(List.of());
    assertThat(csv)
        .isEqualTo("drawId,lotteryType,name,drawDate,winningNumbers,totalTickets,winners\r\n");
  }

  @Test
  void nullListRendersHeaderOnly() {
    String csv = CsvWriter.write(null);
    assertThat(csv).startsWith("drawId,lotteryType,name,drawDate,winningNumbers");
    assertThat(csv).endsWith("\r\n");
  }

  @Test
  void rendersRowWithEscaping() {
    UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
    DrawReportRow row =
        new DrawReportRow(
            id,
            "Mini",
            "Comma, here",
            LocalDateTime.of(2026, 4, 25, 12, 0),
            "Has \"quote\"",
            10L,
            2L);

    String csv = CsvWriter.write(List.of(row));

    String[] lines = csv.split("\r\n");
    assertThat(lines).hasSize(2);
    assertThat(lines[1])
        .contains("11111111-1111-1111-1111-111111111111")
        .contains("Mini")
        .contains("\"Comma, here\"")
        .contains("\"Has \"\"quote\"\"\"")
        .contains("2026-04-25T12:00:00")
        .endsWith("10,2");
  }

  @Test
  void escapesNewlines() {
    DrawReportRow row =
        new DrawReportRow(
            UUID.randomUUID(), "Type", "with\nnewline", LocalDateTime.now(), "1,2,3", 0L, 0L);

    String csv = CsvWriter.write(List.of(row));

    assertThat(csv).contains("\"with\nnewline\"");
    assertThat(csv).contains("\"1,2,3\"");
  }

  @Test
  void handlesNullFields() {
    DrawReportRow row = new DrawReportRow(null, null, null, null, null, 0L, 0L);
    String csv = CsvWriter.write(List.of(row));
    String[] lines = csv.split("\r\n");
    assertThat(lines[1]).isEqualTo(",,,,,0,0");
  }
}
