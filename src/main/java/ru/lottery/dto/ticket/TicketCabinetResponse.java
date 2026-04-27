package ru.lottery.dto.ticket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import ru.lottery.model.enums.TicketStatus;

public record TicketCabinetResponse(
    UUID id,
    UUID drawId,
    List<Integer> numbers,
    TicketStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
