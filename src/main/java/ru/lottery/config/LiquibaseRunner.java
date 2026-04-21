package ru.lottery.config;

import static ru.lottery.util.constant.Env.LIQUIBASE_MIGRATION_SCHEMA;
import static ru.lottery.util.constant.Env.LIQUIBASE_TARGET_SCHEMA;

import java.sql.Connection;

import javax.sql.DataSource;

import ru.lottery.util.EnvUtil;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LiquibaseRunner {
  private static final String LIQUIBASE_SCHEMA = "migration";
  private static final String TARGET_SCHEMA = "public";

  public static void run(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {

      Database database =
          DatabaseFactory.getInstance()
              .findCorrectDatabaseImplementation(new JdbcConnection(connection));

      database.setLiquibaseSchemaName(
          EnvUtil.getOrDefault(LIQUIBASE_MIGRATION_SCHEMA.name(), LIQUIBASE_SCHEMA));
      database.setDefaultSchemaName(
          EnvUtil.getOrDefault(LIQUIBASE_TARGET_SCHEMA.name(), TARGET_SCHEMA));

      Liquibase liquibase =
          new Liquibase("db/changelog-master.yaml", new ClassLoaderResourceAccessor(), database);

      liquibase.update();

    } catch (Exception e) {
      throw new RuntimeException("Liquibase failed", e);
    }
  }
}
