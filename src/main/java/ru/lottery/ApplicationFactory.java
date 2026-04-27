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
import ru.lottery.security.AuthFilter;
import ru.lottery.security.JwtService;
import ru.lottery.security.PasswordService;
import ru.lottery.service.AuthService;
import ru.lottery.service.TicketCabinetService;
import ru.lottery.service.UserService;
import ru.lottery.util.EnvUtil;
import ru.lottery.util.constant.Env;
import ru.lottery.web.AuthServlet;
import ru.lottery.web.HealthServlet;
import ru.lottery.web.UserMeServlet;
import ru.lottery.web.UserTicketsServlet;

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

  private final PasswordService passwordService;
  private final JwtService jwtService;

  private final AuthService authService;
  private final UserService userService;
  private final TicketCabinetService ticketCabinetService;

  private final AuthFilter authFilter;
  private final AuthServlet authServlet;
  private final HealthServlet healthServlet;
  private final UserMeServlet userMeServlet;
  private final UserTicketsServlet userTicketsServlet;

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

    log.info("Building Security services...");
    this.passwordService = new PasswordService();
    this.jwtService =
        new JwtService(
            EnvUtil.get(Env.JWT_SECRET.name()), EnvUtil.getInt(Env.JWT_EXPIRES_MINUTES.name(), 60));

    log.info("Building Application services...");
    this.authService = new AuthService(userRepository, passwordService, jwtService);
    this.userService = new UserService(userRepository);
    this.ticketCabinetService = new TicketCabinetService(ticketRepository);

    this.authFilter = new AuthFilter(jwtService);
    this.authServlet = new AuthServlet(authService);
    this.healthServlet = new HealthServlet();
    this.userMeServlet = new UserMeServlet(userService);
    this.userTicketsServlet = new UserTicketsServlet(ticketCabinetService);

    log.info("Application started successfully");
  }

  private void runLiquibaseScripts() {
    LiquibaseRunner.run(dataSource);
  }
}
