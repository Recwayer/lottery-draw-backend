package ru.lottery.security;

import java.util.List;

import jakarta.inject.Singleton;

import ru.lottery.aop.UserEventAspect;
import ru.lottery.model.User;
import ru.lottery.repository.UserRepository;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.HttpRequestAuthenticationProvider;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class AppAuthenticationProvider implements HttpRequestAuthenticationProvider<Object> {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final UserEventAspect userEventAspect;

  @Override
  public AuthenticationResponse authenticate(
      @Nullable HttpRequest<Object> requestContext,
      AuthenticationRequest<String, String> authRequest) {
    String email = authRequest.getIdentity();
    String password = authRequest.getSecret();

    return userRepository
        .findByEmail(email)
        .filter(user -> passwordHasher.verify(password, user.getPasswordHash()))
        .<AuthenticationResponse>map(this::success)
        .orElseGet(
            () ->
                AuthenticationResponse.failure(
                    AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH));
  }

  private AuthenticationResponse success(User user) {
    userEventAspect.login(user);
    return AuthenticationResponse.success(
        user.getEmail(), List.of("ROLE_" + user.getRole().name()));
  }
}
