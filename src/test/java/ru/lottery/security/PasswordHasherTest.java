package ru.lottery.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

  private final PasswordHasher hasher = new PasswordHasher();

  @Test
  void hashAndVerifyRoundTrip() {
    String hashed = hasher.hash("secret");
    assertThat(hashed).isNotEqualTo("secret");
    assertThat(hasher.verify("secret", hashed)).isTrue();
  }

  @Test
  void verifyReturnsFalseForWrongPassword() {
    String hashed = hasher.hash("secret");
    assertThat(hasher.verify("WRONG", hashed)).isFalse();
  }

  @Test
  void verifyHandlesNullArguments() {
    String hashed = hasher.hash("secret");
    assertThat(hasher.verify(null, hashed)).isFalse();
    assertThat(hasher.verify("secret", null)).isFalse();
    assertThat(hasher.verify(null, null)).isFalse();
  }
}
