package ru.lottery.web;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ru.lottery.util.JsonUtil;

public class HealthServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json");
    resp.setStatus(HttpServletResponse.SC_OK);
    JsonUtil.writeJson(resp, Map.of("status", "UP", "dateTime", LocalDateTime.now()));
  }
}
