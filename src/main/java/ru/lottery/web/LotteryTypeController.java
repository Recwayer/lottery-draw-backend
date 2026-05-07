package ru.lottery.web;

import java.util.List;

import jakarta.validation.Valid;

import ru.lottery.model.LotteryType;
import ru.lottery.service.LotteryTypeService;
import ru.lottery.web.dto.CreateLotteryTypeRequest;
import ru.lottery.web.dto.LotteryTypeResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.validation.Validated;
import lombok.RequiredArgsConstructor;

@Validated
@Controller
@RequiredArgsConstructor
public class LotteryTypeController {

  private final LotteryTypeService lotteryTypeService;

  @Get("/admin/lottery-types")
  @Secured("ROLE_ADMIN")
  public List<LotteryTypeResponse> list() {
    return lotteryTypeService.list().stream().map(LotteryTypeResponse::from).toList();
  }

  @Post(uri = "/admin/lottery-types", consumes = MediaType.APPLICATION_JSON)
  @Secured("ROLE_ADMIN")
  public HttpResponse<LotteryTypeResponse> create(@Body @Valid CreateLotteryTypeRequest request) {
    LotteryType type =
        lotteryTypeService.create(
            request.name(), request.poolMin(), request.poolMax(), request.picks());
    return HttpResponse.created(LotteryTypeResponse.from(type));
  }
}
