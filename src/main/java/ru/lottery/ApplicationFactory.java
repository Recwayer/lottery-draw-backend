package ru.lottery;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;

import ru.lottery.config.DatabaseConfig;
import ru.lottery.config.HibernateConfig;
import ru.lottery.config.LiquibaseRunner;
import ru.lottery.repository.DrawRepository;
import ru.lottery.repository.DrawResultRepository;
import ru.lottery.repository.TicketRepository;
import ru.lottery.repository.UserRepository;
import ru.lottery.web.HealthServlet;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ApplicationFactory {
  private final DataSource dataSource;
  private final SessionFactory sessionFactory;

  private final UserRepository userRepository;
  private final DrawRepository drawRepository;
  private final TicketRepository ticketRepository;
  private final DrawResultRepository drawResultRepository;

  private final HealthServlet healthServlet;

  public ApplicationFactory() {

    log.info("Starting application...");

    this.dataSource = DatabaseConfig.createDataSource();

    log.info("Running Liquibase migrations...");
    runLiquibaseScripts();

    log.info("Building Hibernate SessionFactory...");
    this.sessionFactory = HibernateConfig.buildSessionFactory(dataSource);

    log.info("Building Repositories...");
    this.userRepository = new UserRepository(sessionFactory);
    this.drawRepository = new DrawRepository(sessionFactory);
    this.ticketRepository = new TicketRepository(sessionFactory);
    this.drawResultRepository = new DrawResultRepository(sessionFactory);

    this.healthServlet = new HealthServlet();

    log.info("Application started successfully");
  }

  private void runLiquibaseScripts() {
    LiquibaseRunner.run(dataSource);
  }
}
