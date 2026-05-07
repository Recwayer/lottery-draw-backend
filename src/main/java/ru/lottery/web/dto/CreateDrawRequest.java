package ru.lottery.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record CreateDrawRequest(
    @NotBlank @Size(max = 255) String name,
    @NotNull @Future LocalDateTime drawDate,
    @NotNull UUID lotteryTypeId) {}