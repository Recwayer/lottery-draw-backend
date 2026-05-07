package ru.lottery.support;

import java.util.UUID;

import jakarta.inject.Inject;

import ru.lottery.model.User;
import ru.lottery.model.enums.Role;
import ru.lottery.repository.LotteryTypeRepository;
import ru.lottery.repository.UserRepository;
import ru.lottery.security.PasswordHasher;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

@MicronautTest(environments = "test")
public abstract class AbstractIntegrationTest {

  static {
    PostgresContainer.boot();
  }

  public static final UUID CLASSIC_TYPE_ID =
      UUID.fromString("00000000-0000-0000-0000-00000000c649");

  @Inject protected ApplicationContext ctx;
  @Inject protected UserRepository userRepository;
  @Inject protected LotteryTypeRepository lotteryTypeRepository;
  @Inject protected PasswordHasher passwordHasher;

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
}
