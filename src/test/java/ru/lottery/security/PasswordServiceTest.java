package ru.lottery.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordServiceTest {
  private final PasswordService passwordService = new PasswordService();

  @Test
  void hashAndMatches() {
    String hash = passwordService.hash("Password123!");

    assertThat(hash).isNotEqualTo("Password123!");
    assertThat(passwordService.matches("Password123!", hash)).isTrue();
    assertThat(passwordService.matches("WrongPassword123!", hash)).isFalse();
  }
}
