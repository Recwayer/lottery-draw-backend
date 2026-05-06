package ru.lottery.web;

import java.security.Principal;

import jakarta.validation.Valid;

import ru.lottery.aop.RecordUserEvent;
import ru.lottery.aop.UserSource;
import ru.lottery.model.User;
import ru.lottery.model.enums.UserEventType;
import ru.lottery.repository.RefreshTokenRepository;
import ru.lottery.service.UserService;
import ru.lottery.web.dto.LogoutRequest;
import ru.lottery.web.dto.RegisterRequest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.validator.RefreshTokenValidator;
import io.micronaut.validation.Validated;
import lombok.RequiredArgsConstructor;

@Validated
@Controller
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final RefreshTokenValidator refreshTokenValidator;

  @Post(uri = "/register", consumes = MediaType.APPLICATION_JSON, produces = MediaType.TEXT_PLAIN)
  @Secured(SecurityRule.IS_ANONYMOUS)
  public HttpResponse<String> register(@Body @Valid RegisterRequest request) {
    User user = userService.register(request.email(), request.password());
    return HttpResponse.created(user.getEmail());
  }

  @Post(uri = "/logout", consumes = MediaType.APPLICATION_JSON)
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @RecordUserEvent(
      value = UserEventType.LOGOUT,
      userFrom = UserSource.NAMED_PRINCIPAL_ARG,
      userArg = "principal")
  public HttpResponse<Void> logout(@Body @Valid LogoutRequest request, Principal principal) {
    String storedKey =
        refreshTokenValidator
            .validate(request.refreshToken())
            .orElseThrow(IllegalArgumentException::new);
    refreshTokenRepository.revokeByToken(storedKey);
    return HttpResponse.noContent();
  }
}
