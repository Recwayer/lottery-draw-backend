package ru.lottery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import ru.lottery.model.User;
import ru.lottery.model.UserEvent;
import ru.lottery.model.enums.UserEventType;
import ru.lottery.support.AbstractIntegrationTest;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;

class UserEventRepositoryIT extends AbstractIntegrationTest {

  @Inject UserEventRepository userEventRepository;

  private void seedEvent(User user, UserEventType type, String payload) {
    UserEvent e = new UserEvent();
    e.setUser(user);
    e.setType(type);
    e.setPayload(payload);
    userEventRepository.save(e);
  }

  @Test
  void findByUserIdReturnsPaginated() {
    User u = seedUser("a@b", "pw");
    seedEvent(u, UserEventType.LOGIN, "1");
    seedEvent(u, UserEventType.LOGOUT, "2");
    seedEvent(u, UserEventType.BUY_TICKET, "3");

    Page<UserEvent> page =
        userEventRepository.findByUserId(
            u.getId(), Pageable.from(0, 2, Sort.of(Sort.Order.desc("createdAt"))));
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalSize()).isEqualTo(3L);
  }

  @Test
  void findByUserIdAndTypeInFilters() {
    User u = seedUser("filter@b", "pw");
    seedEvent(u, UserEventType.LOGIN, "1");
    seedEvent(u, UserEventType.BUY_TICKET, "2");
    seedEvent(u, UserEventType.LOGOUT, "3");

    Page<UserEvent> page =
        userEventRepository.findByUserIdAndTypeIn(
            u.getId(),
            Set.of(UserEventType.LOGIN, UserEventType.LOGOUT),
            Pageable.from(0, 10, Sort.of(Sort.Order.desc("createdAt"))));
    assertThat(page.getContent()).hasSize(2);
  }

  @Test
  void findByUserIdAndTypeOrderByCreatedAtDesc() {
    User u = seedUser("recent@b", "pw");
    seedEvent(u, UserEventType.NOTIFICATION_SENT, "n1");
    seedEvent(u, UserEventType.NOTIFICATION_SENT, "n2");

    List<UserEvent> events =
        userEventRepository.findByUserIdAndTypeOrderByCreatedAtDesc(
            u.getId(), UserEventType.NOTIFICATION_SENT, Pageable.from(0, 10));
    assertThat(events).hasSize(2);
  }
}
