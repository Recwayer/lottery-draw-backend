package ru.lottery.dto.auth;

import java.util.UUID;

import ru.lottery.model.enums.Role;

public record AuthUserResponse(UUID id, String email, Role role) {}
