package ru.lottery.exception;

import jakarta.servlet.http.HttpServletResponse;

public class ForbiddenException extends ApiException {
  public ForbiddenException(String message) {
    super(ErrorCode.FORBIDDEN, HttpServletResponse.SC_FORBIDDEN, message);
  }
}
