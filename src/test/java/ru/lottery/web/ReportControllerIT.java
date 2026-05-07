package ru.lottery.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import ru.lottery.model.Draw;
import ru.lottery.service.DrawService;
import ru.lottery.service.TicketService;
import ru.lottery.support.AbstractWebIntegrationTest;
import ru.lottery.web.dto.DrawReportRow;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.security.token.render.BearerAccessRefreshToken;

class ReportControllerIT extends AbstractWebIntegrationTest {

  @Test
  void jsonReportShape() {
    DrawService drawService = bean(DrawService.class);
    TicketService ticketService = bean(TicketService.class);
    seedAdmin("a@b", "password");
    seedUser("u@b", "password");
    BearerAccessRefreshToken admin = login("a@b", "password");
    Draw draw = drawService.create("R1", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());
    ticketService.buy("u@b", draw.getId());
    drawService.runDraw(draw.getId());

    List<DrawReportRow> rows =
        httpClient
            .toBlocking()
            .retrieve(
                HttpRequest.GET("/admin/reports/draws").bearerAuth(admin.getAccessToken()),
                Argument.listOf(DrawReportRow.class));
    assertThat(rows).extracting(DrawReportRow::name).contains("R1");
    assertThat(rows).extracting(DrawReportRow::winningNumbers).doesNotContainNull();
  }

  @Test
  void csvReportContentType() {
    DrawService drawService = bean(DrawService.class);
    TicketService ticketService = bean(TicketService.class);
    seedAdmin("a@b", "password");
    seedUser("u@b", "password");
    BearerAccessRefreshToken admin = login("a@b", "password");
    Draw draw =
        drawService.create("R1\"comma,test", LocalDateTime.now().plusHours(1), CLASSIC_TYPE_ID);
    drawService.start(draw.getId());
    ticketService.buy("u@b", draw.getId());
    drawService.runDraw(draw.getId());

    HttpResponse<String> resp =
        httpClient
            .toBlocking()
            .exchange(
                HttpRequest.GET("/admin/reports/draws?format=csv")
                    .bearerAuth(admin.getAccessToken()),
                String.class);
    String contentType = resp.getContentType().map(Object::toString).orElse("");
    assertThat(contentType).startsWith("text/csv");
    String body = resp.body();
    assertThat(body)
        .startsWith("drawId,lotteryType,name,drawDate,winningNumbers,totalTickets,winners");
    assertThat(body).contains("\"R1\"\"comma,test\"");
  }
}
