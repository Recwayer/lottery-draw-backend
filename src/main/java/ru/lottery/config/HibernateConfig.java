package ru.lottery.config;

import static ru.lottery.util.constant.Env.*;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ru.lottery.model.Draw;
import ru.lottery.model.DrawResult;
import ru.lottery.model.Ticket;
import ru.lottery.model.User;
import ru.lottery.util.EnvUtil;

import lombok.experimental.UtilityClass;

@UtilityClass
public class HibernateConfig {
  private static final String TARGET_SCHEMA = "public";

  public static SessionFactory buildSessionFactory(DataSource dataSource) {

    Configuration cfg = new Configuration();

    cfg.addAnnotatedClass(User.class);
    cfg.addAnnotatedClass(Draw.class);
    cfg.addAnnotatedClass(Ticket.class);
    cfg.addAnnotatedClass(DrawResult.class);

    cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
    cfg.setProperty("hibernate.show_sql", EnvUtil.getBoolean(HIBERNATE_SHOW_SQL.name(), true));
    cfg.setProperty("hibernate.format_sql", EnvUtil.getBoolean(HIBERNATE_FORMAT_SQL.name(), true));

    cfg.setProperty(
        "hibernate.hbm2ddl.auto",
        EnvUtil.getOrDefault(HIBERNATE_DDL_AUTO_STRATEGY.name(), "validate"));

    cfg.getProperties().put("hibernate.connection.datasource", dataSource);

    cfg.setProperty(
        "hibernate.default_schema",
        EnvUtil.getOrDefault(HIBERNATE_DEFAULT_SCHEMA.name(), TARGET_SCHEMA));

    return cfg.buildSessionFactory();
  }
}
