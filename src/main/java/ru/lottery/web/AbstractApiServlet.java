package ru.lottery.web;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import ru.lottery.exception.ValidationException;
import ru.lottery.util.JsonUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public abstract class AbstractApiServlet extends HttpServlet {

  protected <T> T readBody(HttpServletRequest request, Class<T> type) {
    try {
      T body = JsonUtil.parseBody(request, type);
      if (body == null) {
        throw new ValidationException("Request body is required");
      }
      return body;
    } catch (JsonMappingException e) {
      throw new ValidationException("Request body has invalid JSON structure");
    } catch (JsonProcessingException e) {
      throw new ValidationException("Request body must be valid JSON");
    } catch (IOException e) {
      throw new ValidationException("Request body cannot be read");
    }
  }

  protected void writeJson(HttpServletResponse response, int status, Object body)
      throws IOException {
    response.setStatus(status);
    JsonUtil.writeJson(response, body);
  }

  protected void writeError(HttpServletResponse response, Exception exception, Logger logger)
      throws IOException {
    ErrorResponseWriter.writeException(response, exception, logger);
  }
}
