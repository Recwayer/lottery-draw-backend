package ru.lottery.util;

import java.util.Set;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class EnvUtil {
  private static final Set<String> SECRET_PATTERNS =
      Set.of("PASSWORD", "PASS", "SECRET", "KEY", "TOKEN");

  public String get(String name) {
    String value = getValue(name);
    if (!isValidValue(value)) {
      throw new IllegalStateException("Environment variable not found: " + name);
    }
    return value;
  }

  public String getOrDefault(String name, String defaultValue) {
    String value = getValue(name);
    return isValidValue(value) ? value : defaultValue;
  }

  public int getInt(String name, int defaultValue) {
    String value = getValue(name);

    if (!isValidValue(value)) {
      return defaultValue;
    }

    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      log.warn("Invalid integer for env {}: {}", name, value);
      return defaultValue;
    }
  }

  public boolean getBoolean(String name, boolean defaultValue) {
    String value = getValue(name);

    if (!isValidValue(value)) {
      return defaultValue;
    }

    if (!value.equalsIgnoreCase(Boolean.TRUE.toString())
        && !value.equalsIgnoreCase(Boolean.FALSE.toString())) {
      log.warn("Invalid boolean for env {}: {}", name, value);
      return defaultValue;
    }

    return Boolean.parseBoolean(value);
  }

  private String getValue(String name) {
    String value = System.getenv(name);

    if (isSecret(name)) {
      log.debug("Environment {} value: ***masked***", name);
    } else {
      log.debug("Environment {} value: {}", name, value);
    }

    return value;
  }

  private boolean isValidValue(String value) {
    return value != null && !value.isBlank();
  }

  private boolean isSecret(String name) {
    return SECRET_PATTERNS.stream().anyMatch(name::contains);
  }
}
