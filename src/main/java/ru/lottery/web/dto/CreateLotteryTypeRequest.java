package ru.lottery.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record CreateLotteryTypeRequest(
    @NotBlank @Size(max = 63) String name,
    @Min(1) int poolMin,
    @Min(2) int poolMax,
    @Min(1) int picks) {}
