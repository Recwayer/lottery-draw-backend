package ru.lottery.web.dto;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record LogoutRequest(@JsonProperty("refresh_token") @NotBlank String refreshToken) {}
