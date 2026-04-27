package ru.lottery.dto;

import java.util.List;

public record ListResponse<T>(List<T> items) {}
