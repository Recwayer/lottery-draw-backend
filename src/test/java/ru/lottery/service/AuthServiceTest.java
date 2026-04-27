package ru.lottery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.lottery.dto.auth.AuthResponse;
import ru.lottery.dto.auth.LoginRequest;
import ru.lottery.dto.auth.RegisterRequest;
import ru.lottery.dto.user.UserResponse;
import ru.lottery.exception.ConflictException;
import ru.lottery.exception.UnauthorizedException;
import ru.lottery.model.User;
import ru.lottery.model.enums.Role;
import ru.lottery.repository.UserRepository;
import ru.lottery.security.JwtService;
import ru.lottery.security.PasswordService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private PasswordService passwordService;
  @Mock private JwtService jwtService;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(userRepository, passwordService, jwtService);
  }

  @Test
  void registerSuccess() {
    when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
    when(passwordService.hash("Password123!")).thenReturn("bcrypt-hash");
    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(UUID.randomUUID());
              user.setCreatedAt(LocalDateTime.now());
              user.setUpdatedAt(LocalDateTime.now());
              return null;
            })
        .when(userRepository)
        .save(any(User.class));

    UserResponse response =
        authService.register(new RegisterRequest("User@Example.com", "Password123!"));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertThat(response.email()).isEqualTo("user@example.com");
    assertThat(response.role()).isEqualTo(Role.USER);
    assertThat(savedUser.getPasswordHash()).isEqualTo("bcrypt-hash");
    assertThat(savedUser.getPasswordHash()).isNotEqualTo("Password123!");
  }

  @Test
  void registerDuplicateEmail() {
    when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

    assertThatThrownBy(
            () -> authService.register(new RegisterRequest("user@example.com", "Password123!")))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void loginSuccess() {
    User user = User.create("user@example.com", "bcrypt-hash", Role.USER);
    user.setId(UUID.randomUUID());
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordService.matches("Password123!", "bcrypt-hash")).thenReturn(true);
    when(jwtService.generateToken(user)).thenReturn("jwt-token");
    when(jwtService.getExpiresInSeconds()).thenReturn(3600L);

    AuthResponse response = authService.login(new LoginRequest("user@example.com", "Password123!"));

    assertThat(response.accessToken()).isEqualTo("jwt-token");
    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.expiresIn()).isEqualTo(3600);
    assertThat(response.user().id()).isEqualTo(user.getId());
    assertThat(response.user().email()).isEqualTo("user@example.com");
    assertThat(response.user().role()).isEqualTo(Role.USER);
  }

  @Test
  void loginInvalidPassword() {
    User user = User.create("user@example.com", "bcrypt-hash", Role.USER);
    user.setId(UUID.randomUUID());
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordService.matches("bad-password", "bcrypt-hash")).thenReturn(false);

    assertThatThrownBy(
            () -> authService.login(new LoginRequest("user@example.com", "bad-password")))
        .isInstanceOf(UnauthorizedException.class);
  }
}
