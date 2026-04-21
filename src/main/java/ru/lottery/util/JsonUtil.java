package ru.lottery.util;

import java.io.BufferedReader;
import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class JsonUtil {
  @Getter
  private static final ObjectMapper mapper =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  public static <T> T parseBody(HttpServletRequest req, Class<T> clazz) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader = req.getReader()) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    }
    return mapper.readValue(sb.toString(), clazz);
  }

  public static void writeJson(HttpServletResponse resp, Object data) throws IOException {
    resp.setContentType("application/json");
    mapper.writeValue(resp.getWriter(), data);
  }
}
