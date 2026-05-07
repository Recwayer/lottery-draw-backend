package ru.lottery.web.dto;

import java.util.UUID;

import ru.lottery.model.enums.TicketStatus;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record NotificationPayload(
        UUID drawId,
        UUID ticketId,
        String drawName,
        String lotteryType,
        String numbers,
        String winningNumbers,
        TicketStatus status,
        String message) {}