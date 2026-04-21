package ru.lottery.config;

import static ru.lottery.util.constant.Env.*;

import javax.sql.DataSource;

import ru.lottery.util.EnvUtil;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatabaseConfig {

  public static DataSource createDataSource() {

    HikariConfig config = new HikariConfig();

    config.setJdbcUrl(EnvUtil.get(DB_URL.name()));

    config.setUsername(EnvUtil.get(DB_USER.name()));

    config.setPassword(EnvUtil.get(DB_PASSWORD.name()));

    config.setDriverClassName("org.postgresql.Driver");

    config.setMaximumPoolSize(EnvUtil.getInt(DB_POOL_SIZE.name(), 10));

    config.setMinimumIdle(EnvUtil.getInt(DB_POOL_MIN_IDLE.name(), 2));

    config.setIdleTimeout(EnvUtil.getInt(DB_POOL_IDLE_TIMEOUT.name(), 30000));

    config.setConnectionTimeout(EnvUtil.getInt(DB_CONNECTION_TIMEOUT.name(), 10000));

    return new HikariDataSource(config);
  }
}
