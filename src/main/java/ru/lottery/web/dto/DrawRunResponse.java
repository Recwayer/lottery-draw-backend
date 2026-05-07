package ru.lottery.web.dto;

import java.util.UUID;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record DrawRunResponse(UUID drawId, String winningNumbers, int total, int winners) {}
