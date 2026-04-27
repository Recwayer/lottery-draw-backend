package ru.lottery.exception;

import jakarta.servlet.http.HttpServletResponse;

public class UnauthorizedException extends ApiException {
  public UnauthorizedException(String message) {
    super(ErrorCode.UNAUTHORIZED, HttpServletResponse.SC_UNAUTHORIZED, message);
  }
}
