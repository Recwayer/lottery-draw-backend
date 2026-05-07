package ru.lottery.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record DrawReportRow(
    UUID drawId,
    String lotteryType,
    String name,
    LocalDateTime drawDate,
    String winningNumbers,
    long totalTickets,
    long winners) {}
