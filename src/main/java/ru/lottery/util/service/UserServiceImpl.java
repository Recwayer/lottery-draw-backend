package ru.lottery.unit.service;

import ru.lottery.exception.AlreadyExistsException;
import ru.lottery.exception.InvalidCredentialsException;
import ru.lottery.mapper.UserMapper;
import ru.lottery.model.User;
import ru.lottery.model.dto.AuthResponse;
import ru.lottery.model.dto.LoginRequest;
import ru.lottery.model.dto.RegisterRequest;
import ru.lottery.model.enums.Role;
import ru.lottery.repository.UserRepository;
import ru.lottery.util.JwtUtil;
import ru.lottery.util.PasswordUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public void register(RegisterRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new AlreadyExistsException("User with this email already exists");
    }
    User user = userMapper.toEntity(request);
    user.setPasswordHash(PasswordUtil.hash(request.getPassword()));
    user.setRole(Role.USER);
    userRepository.save(user);
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

    if (!PasswordUtil.verify(request.getPassword(), user.getPasswordHash())) {
      throw new InvalidCredentialsException("Invalid email or password");
    }

    String token = JwtUtil.generateToken(user.getEmail(), user.getRole().name());
    return new AuthResponse(token, user.getRole().name());
  }
}
