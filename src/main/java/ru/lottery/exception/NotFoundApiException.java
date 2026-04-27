package ru.lottery.exception;

import jakarta.servlet.http.HttpServletResponse;

public class NotFoundApiException extends ApiException {
  public NotFoundApiException(String message) {
    super(ErrorCode.NOT_FOUND, HttpServletResponse.SC_NOT_FOUND, message);
  }
}
