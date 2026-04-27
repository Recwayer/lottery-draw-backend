package ru.lottery.unit.controller;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ru.lottery.model.dto.AuthResponse;
import ru.lottery.model.dto.LoginRequest;
import ru.lottery.model.dto.RegisterRequest;
import ru.lottery.unit.service.UserService;
import ru.lottery.util.JsonUtil;

public class AuthServlet extends HttpServlet {
  private final UserService userService;

  public AuthServlet(UserService userService) {
    this.userService = userService;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String path = req.getPathInfo();
    resp.setContentType("application/json");

    try {
      if ("/register".equals(path)) {
        RegisterRequest request =
            JsonUtil.MAPPER.readValue(req.getInputStream(), RegisterRequest.class);
        userService.register(request);
        resp.setStatus(HttpServletResponse.SC_CREATED);
      } else if ("/login".equals(path)) {
        LoginRequest request = JsonUtil.MAPPER.readValue(req.getInputStream(), LoginRequest.class);
        AuthResponse response = userService.login(request);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(JsonUtil.MAPPER.writeValueAsString(response));
      } else {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write(JsonUtil.MAPPER.writeValueAsString(Map.of("error", e.getMessage())));
    }
  }
}
