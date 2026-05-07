package ru.lottery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.lottery.model.User;
import ru.lottery.model.UserEvent;
import ru.lottery.model.enums.UserEventType;
import ru.lottery.repository.UserEventRepository;
import ru.lottery.repository.UserRepository;

import io.micronaut.serde.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserEventRecorderTest {

  @Mock UserEventRepository eventRepo;
  @Mock UserRepository userRepo;
  @Mock ObjectMapper objectMapper;

  UserEventRecorder recorder;

  @BeforeEach
  void setUp() {
    recorder = new UserEventRecorder(eventRepo, userRepo, objectMapper);
  }

  private static User user(String email) {
    User u = new User();
    u.setEmail(email);
    return u;
  }

  @Test
  void recordSkipsWhenUserNull() {
    assertThat(recorder.record(null, UserEventType.LOGIN, Map.of())).isNull();
    verify(eventRepo, never()).save(any());
  }

  @Test
  void recordSerializesObjectPayload() throws Exception {
    User u = user("a@b");
    when(objectMapper.writeValueAsString(any())).thenReturn("{\"k\":1}");
    when(eventRepo.save(any(UserEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    UserEvent saved = recorder.record(u, UserEventType.LOGIN, Map.of("k", 1));

    assertThat(saved).isNotNull();
    assertThat(saved.getType()).isEqualTo(UserEventType.LOGIN);
    assertThat(saved.getUser()).isSameAs(u);
    assertThat(saved.getPayload()).isEqualTo("{\"k\":1}");
  }

  @Test
  void recordPreservesCharSequencePayload() {
    User u = user("a@b");
    when(eventRepo.save(any(UserEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    UserEvent saved = recorder.record(u, UserEventType.LOGIN, "raw text");

    assertThat(saved.getPayload()).isEqualTo("raw text");
  }

  @Test
  void recordHandlesNullPayload() {
    User u = user("a@b");
    when(eventRepo.save(any(UserEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    UserEvent saved = recorder.record(u, UserEventType.LOGIN, null);

    assertThat(saved.getPayload()).isNull();
  }

  @Test
  void recordSwallowsSerializationException() throws Exception {
    User u = user("a@b");
    when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("boom"));
    when(eventRepo.save(any(UserEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    UserEvent saved = recorder.record(u, UserEventType.LOGIN, new Object());

    assertThat(saved.getPayload()).isNull();
  }

  @Test
  void recordByEmailNullEmail() {
    assertThat(recorder.recordByEmail(null, UserEventType.LOGOUT, "x")).isNull();
    verify(userRepo, never()).findByEmail(any());
  }

  @Test
  void recordByEmailUnknownUser() {
    when(userRepo.findByEmail("ghost")).thenReturn(Optional.empty());
    assertThat(recorder.recordByEmail("ghost", UserEventType.LOGOUT, "x")).isNull();
    verify(eventRepo, never()).save(any());
  }

  @Test
  void recordByEmailDelegatesToRecord() {
    User u = user("a@b");
    u.setId(UUID.randomUUID());
    when(userRepo.findByEmail("a@b")).thenReturn(Optional.of(u));
    when(eventRepo.save(any(UserEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    UserEvent saved = recorder.recordByEmail("a@b", UserEventType.LOGOUT, "x");

    assertThat(saved).isNotNull();
    ArgumentCaptor<UserEvent> captor = ArgumentCaptor.forClass(UserEvent.class);
    verify(eventRepo).save(captor.capture());
    assertThat(captor.getValue().getUser()).isSameAs(u);
  }
}
