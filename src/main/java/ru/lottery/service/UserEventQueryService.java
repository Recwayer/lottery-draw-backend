package ru.lottery.service;

import java.util.Collection;
import java.util.List;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.lottery.model.User;
import ru.lottery.model.UserEvent;
import ru.lottery.model.enums.UserEventType;
import ru.lottery.repository.UserEventRepository;
import ru.lottery.web.dto.UserEventResponse;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.serde.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class UserEventQueryService {

  private static final Logger LOG = LoggerFactory.getLogger(UserEventQueryService.class);

  private final UserEventRepository userEventRepository;
  private final UserService userService;
  private final ObjectMapper objectMapper;

  public Page<UserEventResponse> history(
      String email, int page, int size, Collection<UserEventType> types) {
    User user = userService.getByEmail(email);
    Pageable pageable = Pageable.from(page, size, Sort.of(Sort.Order.desc("createdAt")));
    Page<UserEvent> events =
        (types == null || types.isEmpty())
            ? userEventRepository.findByUserId(user.getId(), pageable)
            : userEventRepository.findByUserIdAndTypeIn(user.getId(), types, pageable);
    return events.map(this::toResponse);
  }

  public List<UserEventResponse> recent(String email, UserEventType type, int limit) {
    User user = userService.getByEmail(email);
    Pageable pageable = Pageable.from(0, Math.max(1, limit), Sort.of(Sort.Order.desc("createdAt")));
    return userEventRepository
        .findByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), type, pageable)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  private UserEventResponse toResponse(UserEvent event) {
    return new UserEventResponse(
        event.getId(), event.getType(), parsePayload(event.getPayload()), event.getCreatedAt());
  }

  private Object parsePayload(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return objectMapper.readValue(raw, Object.class);
    } catch (Exception e) {
      LOG.debug("Failed to parse user_event payload, returning as-is: {}", e.getMessage());
      return raw;
    }
  }
}
