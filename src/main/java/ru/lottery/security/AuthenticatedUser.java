package ru.lottery.security;

import java.util.UUID;

import ru.lottery.model.enums.Role;

public record AuthenticatedUser(UUID userId, String email, Role role) {}
