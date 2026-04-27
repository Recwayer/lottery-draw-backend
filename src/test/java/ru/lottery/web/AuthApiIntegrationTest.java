package ru.lottery.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.UUID;

import javax.sql.DataSource;

import jakarta.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import ru.lottery.config.HibernateConfig;
import ru.lottery.config.LiquibaseRunner;
import ru.lottery.model.Draw;
import ru.lottery.model.Ticket;
import ru.lottery.model.User;
import ru.lottery.model.enums.DrawStatus;
import ru.lottery.model.enums.TicketStatus;
import ru.lottery.repository.DrawRepository;
import ru.lottery.repository.TicketRepository;
import ru.lottery.repository.UserRepository;
import ru.lottery.security.AuthFilter;
import ru.lottery.security.JwtService;
import ru.lottery.security.PasswordService;
import ru.lottery.service.AuthService;
import ru.lottery.service.TicketCabinetService;
import ru.lottery.service.UserService;
import ru.lottery.util.HibernateExecutor;
import ru.lottery.util.JsonUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Testcontainers(disabledWithoutDocker = true)
class AuthApiIntegrationTest {
  private static final String JWT_SECRET = "integration-test-jwt-secret-32-bytes";

  @Container
  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:17-alpine")
          .withDatabaseName("lottery")
          .withUsername("admin")
          .withPassword("password");

  private HikariDataSource dataSource;
  private SessionFactory sessionFactory;
  private Server server;
  private URI baseUri;
  private HttpClient httpClient;
  private UserRepository userRepository;
  private DrawRepository drawRepository;
  private TicketRepository ticketRepository;

  @BeforeEach
  void setUp() throws Exception {
    dataSource = createDataSource();
    createMigrationSchema(dataSource);
    LiquibaseRunner.run(dataSource);
    sessionFactory = HibernateConfig.buildSessionFactory(dataSource);

    userRepository = new UserRepository(sessionFactory);
    drawRepository = new DrawRepository(sessionFactory);
    ticketRepository = new TicketRepository(sessionFactory);

    PasswordService passwordService = new PasswordService();
    JwtService jwtService = new JwtService(JWT_SECRET, 60);
    AuthService authService = new AuthService(userRepository, passwordService, jwtService);
    UserService userService = new UserService(userRepository);
    TicketCabinetService ticketCabinetService = new TicketCabinetService(ticketRepository);

    server = new Server(0);
    ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    context.addServlet(new ServletHolder(new AuthServlet(authService)), "/api/v1/auth/*");
    context.addServlet(new ServletHolder(new UserMeServlet(userService)), "/api/v1/users/me");
    context.addServlet(
        new ServletHolder(new UserTicketsServlet(ticketCabinetService)),
        "/api/v1/users/me/tickets");
    context.addFilter(
        new FilterHolder(new AuthFilter(jwtService)),
        "/api/v1/users/*",
        EnumSet.of(DispatcherType.REQUEST));
    server.setHandler(context);
    server.start();

    int port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    baseUri = URI.create("http://localhost:" + port);
    httpClient = HttpClient.newHttpClient();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (server != null) {
      server.stop();
    }
    if (sessionFactory != null) {
      sessionFactory.close();
    }
    if (dataSource != null) {
      dataSource.close();
    }
  }

  @Test
  void registerLoginAndGetCurrentUser() throws Exception {
    String email = uniqueEmail();

    HttpResponse<String> registerResponse =
        post("/api/v1/auth/register", registerBody(email, "Password123!"));
    assertThat(registerResponse.statusCode()).isEqualTo(201);
    JsonNode registerJson = readJson(registerResponse);
    assertThat(registerJson.get("email").asText()).isEqualTo(email);
    assertThat(registerJson.has("passwordHash")).isFalse();

    String token = login(email, "Password123!");

    HttpResponse<String> meResponse = get("/api/v1/users/me", token);
    assertThat(meResponse.statusCode()).isEqualTo(200);
    JsonNode meJson = readJson(meResponse);
    assertThat(meJson.get("email").asText()).isEqualTo(email);
    assertThat(meJson.get("role").asText()).isEqualTo("USER");
  }

  @Test
  void getCurrentUserWithoutTokenReturns401() throws Exception {
    HttpResponse<String> response = getWithoutToken("/api/v1/users/me");

    assertThat(response.statusCode()).isEqualTo(401);
    assertThat(readJson(response).get("error").asText()).isEqualTo("UNAUTHORIZED");
  }

  @Test
  void myTicketsReturnsOnlyCurrentUserTickets() throws Exception {
    String firstEmail = uniqueEmail();
    String secondEmail = uniqueEmail();
    register(firstEmail, "Password123!");
    register(secondEmail, "Password123!");
    String firstToken = login(firstEmail, "Password123!");

    User firstUser = userRepository.findByEmail(firstEmail).orElseThrow();
    User secondUser = userRepository.findByEmail(secondEmail).orElseThrow();
    Draw draw = Draw.create("Integration draw", DrawStatus.ACTIVE, LocalDateTime.now().plusDays(1));
    drawRepository.save(draw);
    persistTicket(firstUser, draw, "[1,7,13,22,35,49]");
    persistTicket(secondUser, draw, "[2,8,14,23,36,48]");

    HttpResponse<String> response = get("/api/v1/users/me/tickets", firstToken);

    assertThat(response.statusCode()).isEqualTo(200);
    JsonNode items = readJson(response).get("items");
    assertThat(items).hasSize(1);
    assertThat(items.get(0).get("drawId").asText()).isEqualTo(draw.getId().toString());
    assertThat(items.get(0).get("status").asText()).isEqualTo("CREATED");
    assertThat(items.get(0).get("numbers")).hasSize(6);
  }

  private HikariDataSource createDataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(POSTGRES.getJdbcUrl());
    config.setUsername(POSTGRES.getUsername());
    config.setPassword(POSTGRES.getPassword());
    config.setDriverClassName("org.postgresql.Driver");
    return new HikariDataSource(config);
  }

  private void createMigrationSchema(DataSource dataSource) throws Exception {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("create schema if not exists migration");
    }
  }

  private void persistTicket(User user, Draw draw, String numbers) {
    HibernateExecutor.execute(
        sessionFactory,
        session -> {
          User managedUser = session.find(User.class, user.getId());
          Draw managedDraw = session.find(Draw.class, draw.getId());
          session.persist(Ticket.create(managedUser, managedDraw, numbers, TicketStatus.CREATED));
          return null;
        });
  }

  private void register(String email, String password) throws Exception {
    HttpResponse<String> response = post("/api/v1/auth/register", registerBody(email, password));
    assertThat(response.statusCode()).isEqualTo(201);
  }

  private String login(String email, String password) throws Exception {
    HttpResponse<String> response = post("/api/v1/auth/login", registerBody(email, password));
    assertThat(response.statusCode()).isEqualTo(200);
    return readJson(response).get("accessToken").asText();
  }

  private HttpResponse<String> post(String path, String body) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(baseUri.resolve(path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> get(String path, String token) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(baseUri.resolve(path))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> getWithoutToken(String path) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(baseUri.resolve(path)).GET().build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private JsonNode readJson(HttpResponse<String> response) throws Exception {
    return JsonUtil.getMapper().readTree(response.body());
  }

  private String registerBody(String email, String password) {
    return """
        {"email":"%s","password":"%s"}
        """
        .formatted(email, password);
  }

  private String uniqueEmail() {
    return "user-" + UUID.randomUUID() + "@example.com";
  }
}
