package ru.lottery.aop;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import ru.lottery.model.User;
import ru.lottery.model.UserEvent;
import ru.lottery.model.enums.UserEventType;
import ru.lottery.repository.UserEventRepository;
import ru.lottery.support.AbstractIntegrationTest;
import ru.lottery.web.dto.NotificationPayload;

import io.micronaut.data.model.Pageable;

class RecordUserEventInterceptorIT extends AbstractIntegrationTest {

  @Inject UserEventAspect userEventAspect;
  @Inject UserEventRepository userEventRepository;

  @Test
  void loginAspectRecordsUserEvent() {
    User user = seedUser("aop-login@b", "password");

    userEventAspect.login(user);

    List<UserEvent> events =
        userEventRepository.findByUserIdAndTypeOrderByCreatedAtDesc(
            user.getId(), UserEventType.LOGIN, Pageable.from(0, 10));
    assertThat(events).isNotEmpty();
  }

  @Test
  void notificationSentAspectRecordsByEmail() {
    User user = seedUser("aop-notif@b", "password");

    NotificationPayload payload =
        new NotificationPayload(null, null, null, null, null, null, null, "hi");
    userEventAspect.notificationSent(user.getEmail(), payload);

    List<UserEvent> events =
        userEventRepository.findByUserIdAndTypeOrderByCreatedAtDesc(
            user.getId(), UserEventType.NOTIFICATION_SENT, Pageable.from(0, 10));
    assertThat(events).isNotEmpty();
  }

  @Test
  void buyTicketAspectStoresExplicitPayload() {
    User user = seedUser("aop-buy@b", "password");

    Map<String, Object> payload = Map.of("ticketId", "t-123", "numbers", "1,2,3");
    userEventAspect.buyTicket(user, payload);

    List<UserEvent> events =
        userEventRepository.findByUserIdAndTypeOrderByCreatedAtDesc(
            user.getId(), UserEventType.BUY_TICKET, Pageable.from(0, 10));
    assertThat(events).isNotEmpty();
  }
}
