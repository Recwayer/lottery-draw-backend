package ru.lottery.exception;

import jakarta.servlet.http.HttpServletResponse;

public class ConflictException extends ApiException {
  public ConflictException(String message) {
    super(ErrorCode.CONFLICT, HttpServletResponse.SC_CONFLICT, message);
  }
}
