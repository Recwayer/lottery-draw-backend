package ru.lottery.web;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import ru.lottery.dto.error.ErrorResponse;
import ru.lottery.exception.ApiException;
import ru.lottery.exception.ErrorCode;
import ru.lottery.util.JsonUtil;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorResponseWriter {

  public void writeException(HttpServletResponse response, Exception exception, Logger logger)
      throws IOException {
    if (exception instanceof ApiException apiException) {
      write(
          response,
          apiException.getHttpStatus(),
          apiException.getErrorCode(),
          apiException.getMessage());
      return;
    }

    logger.error("Unexpected API error", exception);
    write(
        response,
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        ErrorCode.INTERNAL_ERROR,
        "Internal server error");
  }

  public void write(HttpServletResponse response, int status, ErrorCode errorCode, String message)
      throws IOException {
    response.setStatus(status);
    JsonUtil.writeJson(response, new ErrorResponse(errorCode, message, Instant.now()));
  }
}
