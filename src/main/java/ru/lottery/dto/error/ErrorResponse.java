package ru.lottery.dto.error;

import java.time.Instant;

import ru.lottery.exception.ErrorCode;

public record ErrorResponse(ErrorCode error, String message, Instant timestamp) {}
