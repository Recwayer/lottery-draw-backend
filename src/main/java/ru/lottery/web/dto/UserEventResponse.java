package ru.lottery.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import ru.lottery.model.enums.UserEventType;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record UserEventResponse(
    UUID id, UserEventType type, Object payload, LocalDateTime createdAt) {}
