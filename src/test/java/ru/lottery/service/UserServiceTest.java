package ru.lottery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.lottery.model.User;
import ru.lottery.model.enums.Role;
import ru.lottery.repository.UserRepository;
import ru.lottery.security.PasswordHasher;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock UserRepository userRepository;
  @Mock PasswordHasher passwordHasher;
  @InjectMocks UserService service;

  @Test
  void registerHappyPath() {
    when(userRepository.existsByEmail("a@b.c")).thenReturn(false);
    when(passwordHasher.hash("pw")).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User user = service.register("a@b.c", "pw");

    assertThat(user.getEmail()).isEqualTo("a@b.c");
    assertThat(user.getPasswordHash()).isEqualTo("hashed");
    assertThat(user.getRole()).isEqualTo(Role.USER);
  }

  @Test
  void registerRejectsDuplicate() {
    when(userRepository.existsByEmail("dup@x")).thenReturn(true);
    assertThatThrownBy(() -> service.register("dup@x", "pw"))
        .isInstanceOf(HttpStatusException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.CONFLICT);
    verify(userRepository, never()).save(any());
  }

  @Test
  void getByEmailNotFound() {
    when(userRepository.findByEmail("ghost")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.getByEmail("ghost"))
        .isInstanceOf(HttpStatusException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void findByEmailDelegates() {
    User user = new User();
    when(userRepository.findByEmail("x")).thenReturn(Optional.of(user));
    assertThat(service.findByEmail("x")).contains(user);
  }
}
