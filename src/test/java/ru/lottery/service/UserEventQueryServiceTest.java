package ru.lottery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.lottery.model.User;
import ru.lottery.model.UserEvent;
import ru.lottery.model.enums.UserEventType;
import ru.lottery.repository.UserEventRepository;
import ru.lottery.web.dto.UserEventResponse;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.serde.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserEventQueryServiceTest {

  @Mock UserEventRepository repo;
  @Mock UserService userService;
  @Mock ObjectMapper objectMapper;

  UserEventQueryService service;

  User user;

  @BeforeEach
  void setUp() {
    service = new UserEventQueryService(repo, userService, objectMapper);
    user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail("a@b");
    when(userService.getByEmail("a@b")).thenReturn(user);
  }

  private UserEvent event(UserEventType type, String payload) {
    UserEvent e = new UserEvent();
    e.setId(UUID.randomUUID());
    e.setType(type);
    e.setPayload(payload);
    return e;
  }

  @Test
  void historyWithoutTypesUsesUserIdQuery() throws Exception {
    UserEvent e = event(UserEventType.LOGIN, "{\"k\":1}");
    when(repo.findByUserId(eq(user.getId()), any(Pageable.class)))
        .thenReturn(Page.of(List.of(e), Pageable.from(0, 10), 1L));
    when(objectMapper.readValue("{\"k\":1}", Object.class)).thenReturn(Map.of("k", 1));

    Page<UserEventResponse> page = service.history("a@b", 0, 10, null);

    assertThat(page.getContent()).hasSize(1);
    UserEventResponse r = page.getContent().get(0);
    assertThat(r.type()).isEqualTo(UserEventType.LOGIN);
    assertThat(r.payload()).isEqualTo(Map.of("k", 1));
    verify(repo, never()).findByUserIdAndTypeIn(any(), anyCollection(), any());
  }

  @Test
  void historyWithTypesUsesFilteredQuery() {
    UserEvent e = event(UserEventType.LOGOUT, null);
    when(repo.findByUserIdAndTypeIn(eq(user.getId()), eq(Set.of(UserEventType.LOGOUT)), any()))
        .thenReturn(Page.of(List.of(e), Pageable.from(0, 5), 1L));

    Page<UserEventResponse> page = service.history("a@b", 0, 5, Set.of(UserEventType.LOGOUT));

    assertThat(page.getContent()).hasSize(1);
    assertThat(page.getContent().get(0).payload()).isNull();
    verify(repo, never()).findByUserId(any(), any());
  }

  @Test
  void recentReturnsList() {
    UserEvent e = event(UserEventType.NOTIFICATION_SENT, "");
    e.setCreatedAt(LocalDateTime.now());
    when(repo.findByUserIdAndTypeOrderByCreatedAtDesc(
            eq(user.getId()), eq(UserEventType.NOTIFICATION_SENT), any(Pageable.class)))
        .thenReturn(List.of(e));

    List<UserEventResponse> result = service.recent("a@b", UserEventType.NOTIFICATION_SENT, 0);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).payload()).isNull();
  }

  @Test
  void payloadParsingFallsBackToRawOnError() throws Exception {
    UserEvent e = event(UserEventType.LOGIN, "not-json");
    when(repo.findByUserId(eq(user.getId()), any(Pageable.class)))
        .thenReturn(Page.of(List.of(e), Pageable.from(0, 10), 1L));
    when(objectMapper.readValue("not-json", Object.class)).thenThrow(new RuntimeException("nope"));

    Page<UserEventResponse> page = service.history("a@b", 0, 10, null);

    assertThat(page.getContent().get(0).payload()).isEqualTo("not-json");
  }
}
