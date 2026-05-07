package ru.lottery.web;

import java.security.Principal;
import java.util.List;

import ru.lottery.model.enums.UserEventType;
import ru.lottery.service.UserEventQueryService;
import ru.lottery.web.dto.UserEventResponse;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.RequiredArgsConstructor;

@Controller
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor
public class UserHistoryController {

  private final UserEventQueryService queryService;

  @Get("/me/history")
  public Page<UserEventResponse> history(
      Principal principal,
      @QueryValue(defaultValue = "0") int page,
      @QueryValue(defaultValue = "20") int size,
      @Nullable @QueryValue("type") List<UserEventType> types) {
    return queryService.history(principal.getName(), page, size, types);
  }
}
