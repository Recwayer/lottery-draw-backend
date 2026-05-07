package ru.lottery.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import ru.lottery.web.dto.DrawReportRow;

public final class CsvWriter {

  private static final String[] HEADERS = {
    "drawId", "lotteryType", "name", "drawDate", "winningNumbers", "totalTickets", "winners"
  };

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private CsvWriter() {}

  public static String write(List<DrawReportRow> rows) {
    StringBuilder sb = new StringBuilder();
    appendRow(sb, HEADERS);
    if (rows != null) {
      for (DrawReportRow row : rows) {
        appendRow(
            sb,
            new String[] {
              str(row.drawId()),
              row.lotteryType(),
              row.name(),
              format(row.drawDate()),
              row.winningNumbers(),
              Long.toString(row.totalTickets()),
              Long.toString(row.winners())
            });
      }
    }
    return sb.toString();
  }

  private static void appendRow(StringBuilder sb, String[] values) {
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(escape(values[i]));
    }
    sb.append("\r\n");
  }

  private static String escape(String value) {
    if (value == null) {
      return "";
    }
    boolean needsQuoting =
        value.indexOf(',') >= 0
            || value.indexOf('"') >= 0
            || value.indexOf('\n') >= 0
            || value.indexOf('\r') >= 0;
    if (!needsQuoting) {
      return value;
    }
    StringBuilder sb = new StringBuilder(value.length() + 2);
    sb.append('"');
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '"') {
        sb.append('"');
      }
      sb.append(c);
    }
    sb.append('"');
    return sb.toString();
  }

  private static String str(Object o) {
    return o == null ? "" : o.toString();
  }

  private static String format(LocalDateTime dt) {
    return dt == null ? "" : dt.format(DATE_FORMATTER);
  }
}
