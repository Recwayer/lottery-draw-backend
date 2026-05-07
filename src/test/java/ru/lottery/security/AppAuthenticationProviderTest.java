package ru.lottery.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.lottery.aop.UserEventAspect;
import ru.lottery.model.User;
import ru.lottery.model.enums.Role;
import ru.lottery.repository.UserRepository;

import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;

@ExtendWith(MockitoExtension.class)
class AppAuthenticationProviderTest {

  @Mock UserRepository userRepository;
  @Mock PasswordHasher passwordHasher;
  @Mock UserEventAspect userEventAspect;
  @InjectMocks AppAuthenticationProvider provider;

  private static User user(String email, Role role) {
    User u = new User();
    u.setEmail(email);
    u.setPasswordHash("hash");
    u.setRole(role);
    return u;
  }

  private static AuthenticationRequest<String, String> request(String identity, String secret) {
    return new AuthenticationRequest<>() {
      @Override
      public String getIdentity() {
        return identity;
      }

      @Override
      public String getSecret() {
        return secret;
      }
    };
  }

  @Test
  void successfulAuthenticationRecordsLoginAndReturnsAuthority() {
    User u = user("a@b", Role.USER);
    when(userRepository.findByEmail("a@b")).thenReturn(Optional.of(u));
    when(passwordHasher.verify("pw", "hash")).thenReturn(true);

    AuthenticationResponse resp = provider.authenticate(null, request("a@b", "pw"));

    assertThat(resp.isAuthenticated()).isTrue();
    assertThat(resp.getAuthentication().orElseThrow().getRoles()).containsExactly("ROLE_USER");
    verify(userEventAspect).login(u);
  }

  @Test
  void wrongPasswordReturnsFailure() {
    User u = user("a@b", Role.USER);
    when(userRepository.findByEmail("a@b")).thenReturn(Optional.of(u));
    when(passwordHasher.verify("pw", "hash")).thenReturn(false);

    AuthenticationResponse resp = provider.authenticate(null, request("a@b", "pw"));

    assertThat(resp.isAuthenticated()).isFalse();
    assertThat(resp).isInstanceOf(AuthenticationFailed.class);
    assertThat(((AuthenticationFailed) resp).getReason())
        .isEqualTo(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
    verify(userEventAspect, never()).login(any());
  }

  @Test
  void missingUserReturnsFailure() {
    when(userRepository.findByEmail("ghost")).thenReturn(Optional.empty());

    AuthenticationResponse resp = provider.authenticate(null, request("ghost", "pw"));

    assertThat(resp.isAuthenticated()).isFalse();
    verify(userEventAspect, never()).login(any());
  }
}
