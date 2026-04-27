package ru.lottery.util;

import org.mindrot.jbcrypt.BCrypt;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PasswordUtil {
  public static String hash(String plainPassword) {
    return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
  }

  public static boolean verify(String plainPassword, String hash) {
    return BCrypt.checkpw(plainPassword, hash);
  }
}
