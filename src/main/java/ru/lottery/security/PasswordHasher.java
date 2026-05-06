package ru.lottery.security;

import jakarta.inject.Singleton;

import org.mindrot.jbcrypt.BCrypt;

@Singleton
public class PasswordHasher {

  public String hash(String rawPassword) {
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
  }

  public boolean verify(String rawPassword, String hashedPassword) {
    if (rawPassword == null || hashedPassword == null) {
      return false;
    }
    return BCrypt.checkpw(rawPassword, hashedPassword);
  }
}
