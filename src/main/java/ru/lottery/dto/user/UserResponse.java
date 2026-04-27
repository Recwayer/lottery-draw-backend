package ru.lottery.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

import ru.lottery.model.enums.Role;

public record UserResponse(
    UUID id, String email, Role role, LocalDateTime createdAt, LocalDateTime updatedAt) {}
