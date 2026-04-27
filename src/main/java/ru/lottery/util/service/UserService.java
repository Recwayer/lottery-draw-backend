package ru.lottery.unit.service;

import ru.lottery.model.dto.AuthResponse;
import ru.lottery.model.dto.LoginRequest;
import ru.lottery.model.dto.RegisterRequest;

public interface UserService {
  void register(RegisterRequest request);

  AuthResponse login(LoginRequest request);
}
