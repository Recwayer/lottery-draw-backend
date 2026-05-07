package ru.lottery.web;

import java.security.Principal;
import java.util.List;

import org.reactivestreams.Publisher;

import ru.lottery.model.enums.UserEventType;
import ru.lottery.service.NotificationService;
import ru.lottery.service.UserEventQueryService;
import ru.lottery.web.dto.NotificationPayload;
import ru.lottery.web.dto.UserEventResponse;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.sse.Event;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.RequiredArgsConstructor;

@Controller
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserEventQueryService queryService;

    @Get(uri = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM)
    public Publisher<Event<NotificationPayload>> stream(Principal principal) {
        return notificationService.stream(principal.getName()).map(Event::of);
    }

    @Get("/notifications/recent")
    public List<UserEventResponse> recent(
            Principal principal, @QueryValue(defaultValue = "20") int limit) {
        return queryService.recent(principal.getName(), UserEventType.NOTIFICATION_SENT, limit);
    }
}