package ru.lottery.service;

import java.util.regex.Pattern;

import ru.lottery.dto.auth.AuthResponse;
import ru.lottery.dto.auth.LoginRequest;
import ru.lottery.dto.auth.RegisterRequest;
import ru.lottery.dto.user.UserResponse;
import ru.lottery.exception.ConflictException;
import ru.lottery.exception.UnauthorizedException;
import ru.lottery.exception.ValidationException;
import ru.lottery.mapper.UserMapper;
import ru.lottery.model.User;
import ru.lottery.model.enums.Role;
import ru.lottery.repository.UserRepository;
import ru.lottery.security.JwtService;
import ru.lottery.security.PasswordService;

public class AuthService {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

  private final UserRepository userRepository;
  private final PasswordService passwordService;
  private final JwtService jwtService;

  public AuthService(
      UserRepository userRepository, PasswordService passwordService, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordService = passwordService;
    this.jwtService = jwtService;
  }

  public UserResponse register(RegisterRequest request) {
    String email = normalizeEmail(request.email());
    String password = validatePassword(request.password());

    if (userRepository.existsByEmail(email)) {
      throw new ConflictException("Email is already registered");
    }

    User user = User.create(email, passwordService.hash(password), Role.USER);
    userRepository.save(user);
    return UserMapper.toResponse(user);
  }

  public AuthResponse login(LoginRequest request) {
    String email = normalizeEmail(request.email());
    String password = requirePassword(request.password());

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

    if (!passwordService.matches(password, user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid email or password");
    }

    return new AuthResponse(
        jwtService.generateToken(user),
        "Bearer",
        jwtService.getExpiresInSeconds(),
        UserMapper.toAuthResponse(user));
  }

  private String normalizeEmail(String email) {
    if (email == null || email.isBlank()) {
      throw new ValidationException("Email is required");
    }

    String normalized = email.trim().toLowerCase();
    if (!EMAIL_PATTERN.matcher(normalized).matches()) {
      throw new ValidationException("Email must be valid");
    }
    return normalized;
  }

  private String validatePassword(String password) {
    String value = requirePassword(password);
    if (value.length() < 8) {
      throw new ValidationException("Password must contain at least 8 characters");
    }
    return value;
  }

  private String requirePassword(String password) {
    if (password == null || password.isBlank()) {
      throw new ValidationException("Password is required");
    }
    return password;
  }
}
