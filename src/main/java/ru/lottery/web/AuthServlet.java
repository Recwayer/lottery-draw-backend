package ru.lottery.web;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ru.lottery.dto.auth.LoginRequest;
import ru.lottery.dto.auth.RegisterRequest;
import ru.lottery.exception.NotFoundApiException;
import ru.lottery.service.AuthService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthServlet extends AbstractApiServlet {
  private final AuthService authService;

  public AuthServlet(AuthService authService) {
    this.authService = authService;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    try {
      String path = request.getPathInfo();
      if (path == null) {
        throw new NotFoundApiException("Endpoint not found");
      }

      switch (path) {
        case "/register" ->
            writeJson(
                response,
                HttpServletResponse.SC_CREATED,
                authService.register(readBody(request, RegisterRequest.class)));
        case "/login" ->
            writeJson(
                response,
                HttpServletResponse.SC_OK,
                authService.login(readBody(request, LoginRequest.class)));
        default -> throw new NotFoundApiException("Endpoint not found");
      }
    } catch (Exception e) {
      writeError(response, e, log);
    }
  }
}
