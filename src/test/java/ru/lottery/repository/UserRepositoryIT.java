package ru.lottery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import ru.lottery.model.User;
import ru.lottery.model.enums.Role;
import ru.lottery.support.AbstractIntegrationTest;

class UserRepositoryIT extends AbstractIntegrationTest {

  @Test
  void saveAndFind() {
    User u = new User();
    u.setEmail("a@b.c");
    u.setPasswordHash("hash");
    u.setRole(Role.USER);
    User saved = userRepository.save(u);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();

    Optional<User> found = userRepository.findByEmail("a@b.c");
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
  }

  @Test
  void existsByEmail() {
    User u = new User();
    u.setEmail("x@y.z");
    u.setPasswordHash("hash");
    u.setRole(Role.ADMIN);
    userRepository.save(u);

    assertThat(userRepository.existsByEmail("x@y.z")).isTrue();
    assertThat(userRepository.existsByEmail("missing")).isFalse();
  }
}
