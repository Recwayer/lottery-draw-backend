package ru.lottery.service;

import java.util.Optional;

import jakarta.inject.Singleton;

import ru.lottery.model.User;
import ru.lottery.model.enums.Role;
import ru.lottery.repository.UserRepository;
import ru.lottery.security.PasswordHasher;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;

  public User register(String email, String rawPassword) {
    if (userRepository.existsByEmail(email)) {
      throw new HttpStatusException(HttpStatus.CONFLICT, "User with this email already exists");
    }
    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(passwordHasher.hash(rawPassword));
    user.setRole(Role.USER);
    return userRepository.save(user);
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public User getByEmail(String email) {
    return findByEmail(email)
        .orElseThrow(() -> new HttpStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
  }
}
