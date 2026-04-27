package ru.lottery.exception;

import jakarta.servlet.http.HttpServletResponse;

public class ValidationException extends ApiException {
  public ValidationException(String message) {
    super(ErrorCode.VALIDATION_ERROR, HttpServletResponse.SC_BAD_REQUEST, message);
  }
}
