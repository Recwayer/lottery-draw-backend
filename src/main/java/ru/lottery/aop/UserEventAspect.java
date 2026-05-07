package ru.lottery.aop;

import java.util.Map;

import jakarta.inject.Singleton;

import ru.lottery.model.User;
import ru.lottery.model.enums.UserEventType;
import ru.lottery.web.dto.NotificationPayload;

@Singleton
public class UserEventAspect {

    @RecordUserEvent(
            value = UserEventType.LOGIN,
            userFrom = UserSource.NAMED_USER_ARG,
            userArg = "user")
    public void login(User user) {}

    @RecordUserEvent(
            value = UserEventType.BUY_TICKET,
            userFrom = UserSource.NAMED_USER_ARG,
            userArg = "user",
            payloadArg = "payload")
    public void buyTicket(User user, Map<String, Object> payload) {}

    @RecordUserEvent(
            value = UserEventType.TICKET_WIN,
            userFrom = UserSource.NAMED_USER_ARG,
            userArg = "user",
            payloadArg = "payload")
    public void ticketWin(User user, Map<String, Object> payload) {}

    @RecordUserEvent(
            value = UserEventType.TICKET_LOSE,
            userFrom = UserSource.NAMED_USER_ARG,
            userArg = "user",
            payloadArg = "payload")
    public void ticketLose(User user, Map<String, Object> payload) {}

    @RecordUserEvent(
            value = UserEventType.TICKET_REFUND,
            userFrom = UserSource.NAMED_USER_ARG,
            userArg = "user",
            payloadArg = "payload")
    public void ticketRefund(User user, Map<String, Object> payload) {}

    @RecordUserEvent(
            value = UserEventType.DRAW_CANCELED,
            userFrom = UserSource.NAMED_USER_ARG,
            userArg = "user",
            payloadArg = "payload")
    public void drawCanceled(User user, Map<String, Object> payload) {}

    @RecordUserEvent(
            value = UserEventType.NOTIFICATION_SENT,
            userFrom = UserSource.NAMED_EMAIL_ARG,
            userArg = "email",
            payloadArg = "payload")
    public void notificationSent(String email, NotificationPayload payload) {}
}