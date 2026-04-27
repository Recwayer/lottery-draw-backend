package ru.lottery.web;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ru.lottery.security.AuthFilter;
import ru.lottery.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserMeServlet extends AbstractApiServlet {
  private final UserService userService;

  public UserMeServlet(UserService userService) {
    this.userService = userService;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    try {
      writeJson(
          response,
          HttpServletResponse.SC_OK,
          userService.getCurrentUser(AuthFilter.getAuthenticatedUser(request)));
    } catch (Exception e) {
      writeError(response, e, log);
    }
  }
}
