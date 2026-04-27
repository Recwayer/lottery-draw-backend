package ru.lottery.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {

  public String hash(String rawPassword) {
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
  }

  public boolean matches(String rawPassword, String passwordHash) {
    if (rawPassword == null || passwordHash == null || passwordHash.isBlank()) {
      return false;
    }
    return BCrypt.checkpw(rawPassword, passwordHash);
  }
}
