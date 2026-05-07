package ru.lottery.service;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.lottery.model.User;
import ru.lottery.model.UserEvent;
import ru.lottery.model.enums.UserEventType;
import ru.lottery.repository.UserEventRepository;
import ru.lottery.repository.UserRepository;

import io.micronaut.serde.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class UserEventRecorder {

    private static final Logger LOG = LoggerFactory.getLogger(UserEventRecorder.class);

    private final UserEventRepository userEventRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserEvent record(User user, UserEventType type, Object payload) {
        if (user == null) {
            LOG.warn("Skip recording {} event: user is null", type);
            return null;
        }
        UserEvent event = new UserEvent();
        event.setUser(user);
        event.setType(type);
        event.setPayload(serialize(payload));
        return userEventRepository.save(event);
    }

    public UserEvent recordByEmail(String email, UserEventType type, Object payload) {
        if (email == null) {
            LOG.warn("Skip recording {} event: email is null", type);
            return null;
        }
        return userRepository
                .findByEmail(email)
                .map(user -> record(user, type, payload))
                .orElseGet(
                        () -> {
                            LOG.warn("Skip recording {} event: user not found by email={}", type, email);
                            return null;
                        });
    }

    private String serialize(Object payload) {
        if (payload == null) {
            return null;
        }
        if (payload instanceof CharSequence cs) {
            return cs.toString();
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            LOG.warn("Failed to serialize event payload of type {}", payload.getClass(), e);
            return null;
        }
    }
}