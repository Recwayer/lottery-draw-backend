package ru.lottery.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record RegisterRequest(
    @NotBlank @Email String email, @NotBlank @Size(min = 8, max = 100) String password) {}
