package ru.lottery.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import ru.lottery.model.User;
import ru.lottery.model.enums.Role;
import ru.lottery.repository.LotteryTypeRepository;
import ru.lottery.repository.UserRepository;
import ru.lottery.security.PasswordHasher;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.render.BearerAccessRefreshToken;

public abstract class AbstractWebIntegrationTest {

  static {
    PostgresContainer.boot();
  }

  public static final UUID CLASSIC_TYPE_ID =
      UUID.fromString("00000000-0000-0000-0000-00000000c649");

  protected static EmbeddedServer embeddedServer;
  protected static ApplicationContext ctx;
  protected static UserRepository userRepository;
  protected static LotteryTypeRepository lotteryTypeRepository;
  protected static PasswordHasher passwordHasher;
  protected static HttpClient httpClient;

  @BeforeAll
  static void startServer() {
    if (embeddedServer == null) {
      embeddedServer = ApplicationContext.run(EmbeddedServer.class, "test");
      ctx = embeddedServer.getApplicationContext();
      userRepository = ctx.getBean(UserRepository.class);
      lotteryTypeRepository = ctx.getBean(LotteryTypeRepository.class);
      passwordHasher = ctx.getBean(PasswordHasher.class);
      httpClient = ctx.createBean(HttpClient.class, embeddedServer.getURL());
      Runtime.getRuntime().addShutdownHook(new Thread(embeddedServer::stop));
    }
  }

  @BeforeEach
  void cleanDatabase() throws SQLException {
    try (Connection c =
            DriverManager.getConnection(
                PostgresContainer.INSTANCE.getJdbcUrl(),
                PostgresContainer.INSTANCE.getUsername(),
                PostgresContainer.INSTANCE.getPassword());
        Statement s = c.createStatement()) {
      s.execute(
          "TRUNCATE TABLE user_event, ticket, refresh_token, draw_result, draw, \"user\""
              + " RESTART IDENTITY CASCADE");
      s.execute("DELETE FROM lottery_type WHERE id <> '" + CLASSIC_TYPE_ID + "'");
    }
  }

  protected static <T> T bean(Class<T> type) {
    return ctx.getBean(type);
  }

  protected User seedUser(String email, String rawPassword) {
    return seed(email, rawPassword, Role.USER);
  }

  protected User seedAdmin(String email, String rawPassword) {
    return seed(email, rawPassword, Role.ADMIN);
  }

  private User seed(String email, String rawPassword, Role role) {
    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(passwordHasher.hash(rawPassword));
    user.setRole(role);
    return userRepository.save(user);
  }

  protected BearerAccessRefreshToken login(String email, String password) {
    return httpClient
        .toBlocking()
        .retrieve(
            HttpRequest.POST("/login", new UsernamePasswordCredentials(email, password)),
            Argument.of(BearerAccessRefreshToken.class));
  }
}
