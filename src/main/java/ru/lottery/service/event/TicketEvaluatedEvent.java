package ru.lottery.service.event;

import java.util.UUID;

import ru.lottery.model.enums.TicketStatus;

public record TicketEvaluatedEvent(
    UUID drawId,
    String drawName,
    String lotteryType,
    UUID ticketId,
    String ownerEmail,
    String numbers,
    String winningNumbers,
    TicketStatus status) {}