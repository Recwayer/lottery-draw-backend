package ru.lottery.util;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtil {
  public static final ObjectMapper MAPPER = new JsonMapper().registerModule(new JavaTimeModule());

  public static void writeJson(HttpServletResponse response, Object object) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    MAPPER.writeValue(response.getOutputStream(), object);
  }
}
