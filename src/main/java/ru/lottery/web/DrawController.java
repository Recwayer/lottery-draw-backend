package ru.lottery.web;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import ru.lottery.model.Draw;
import ru.lottery.service.DrawService;
import ru.lottery.web.dto.CreateDrawRequest;
import ru.lottery.web.dto.DrawResponse;
import ru.lottery.web.dto.DrawRunResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import lombok.RequiredArgsConstructor;

@Validated
@Controller
@RequiredArgsConstructor
public class DrawController {

  private final DrawService drawService;

  @Get("/draws/active")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public List<DrawResponse> listActive() {
    return drawService.listActive().stream().map(DrawResponse::from).toList();
  }

  @Post(uri = "/admin/draws", consumes = MediaType.APPLICATION_JSON)
  @Secured("ROLE_ADMIN")
  public HttpResponse<DrawResponse> create(@Body @Valid CreateDrawRequest request) {
    Draw draw = drawService.create(request.name(), request.drawDate(), request.lotteryTypeId());
    return HttpResponse.created(DrawResponse.from(draw));
  }

  @Post("/admin/draws/{id}/start")
  @Secured("ROLE_ADMIN")
  public DrawResponse start(@PathVariable UUID id) {
    return DrawResponse.from(drawService.start(id));
  }

  @Post("/admin/draws/{id}/run")
  @Secured("ROLE_ADMIN")
  public DrawRunResponse run(@PathVariable UUID id) {
    return drawService.runDraw(id);
  }

  @Post("/admin/draws/{id}/cancel")
  @Secured("ROLE_ADMIN")
  public DrawResponse cancel(@PathVariable UUID id) {
    return DrawResponse.from(drawService.cancel(id));
  }
}
