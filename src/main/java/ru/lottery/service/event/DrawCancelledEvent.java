package ru.lottery.service.event;

import java.util.List;
import java.util.UUID;

public record DrawCancelledEvent(
    UUID drawId, String drawName, List<UUID> ticketIds, List<String> ownerEmails) {}