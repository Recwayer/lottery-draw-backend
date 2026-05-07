package ru.lottery.web;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ru.lottery.service.ReportService;
import ru.lottery.util.CsvWriter;
import ru.lottery.web.dto.DrawReportRow;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @Get(
      uri = "/admin/reports/draws",
      produces = {MediaType.APPLICATION_JSON, "text/csv"})
  @Secured("ROLE_ADMIN")
  public HttpResponse<?> finishedDraws(
      @Nullable @QueryValue LocalDateTime from,
      @Nullable @QueryValue LocalDateTime to,
      @Nullable @QueryValue UUID type,
      @QueryValue(defaultValue = "json") String format) {
    List<DrawReportRow> rows =
        reportService.finishedDraws(
            Optional.ofNullable(from), Optional.ofNullable(to), Optional.ofNullable(type));
    if ("csv".equalsIgnoreCase(format)) {
      String csv = CsvWriter.write(rows);
      return HttpResponse.ok(csv)
          .contentType("text/csv; charset=utf-8")
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=draws.csv");
    }
    return HttpResponse.ok(rows).contentType(MediaType.APPLICATION_JSON);
  }
}
