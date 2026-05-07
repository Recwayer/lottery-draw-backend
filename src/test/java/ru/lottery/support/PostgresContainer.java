package ru.lottery.support;

import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresContainer {

  public static final PostgreSQLContainer<?> INSTANCE =
      new PostgreSQLContainer<>("postgres:17-alpine")
          .withDatabaseName("lottery")
          .withUsername("test")
          .withPassword("test")
          .withReuse(false);

  static {
    INSTANCE.start();
    Runtime.getRuntime().addShutdownHook(new Thread(INSTANCE::stop));
    System.setProperty("DB_URL", INSTANCE.getJdbcUrl());
    System.setProperty("DB_USER", INSTANCE.getUsername());
    System.setProperty("DB_PASSWORD", INSTANCE.getPassword());
    System.setProperty("DB_DEFAULT_SCHEMA", "public");
    System.setProperty("DB_MIGRATION_SCHEMA", "public");
    System.setProperty("DB_POOL_SIZE", "5");
    System.setProperty("DB_POOL_MIN_IDLE", "1");
    System.setProperty("DB_POOL_IDLE_TIMEOUT", "30000");
    System.setProperty("DB_CONNECTION_TIMEOUT", "10000");
    System.setProperty("JWT_SECRET", "pleaseChangeThisSecretAtLeast32CharactersLong!");
    System.setProperty("JWT_REFRESH_SECRET", "pleaseChangeThisRefreshSecretAtLeast32Chars!");
    System.setProperty("JWT_ACCESS_TTL_SECONDS", "900");
    System.setProperty("JWT_REFRESH_TTL_SECONDS", "604800");
  }

  public static void boot() {
    if (!INSTANCE.isRunning()) {
      INSTANCE.start();
    }
  }

  private PostgresContainer() {}
}
