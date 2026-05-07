package ru.lottery.aop;

import java.security.Principal;
import java.util.Map;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.lottery.model.User;
import ru.lottery.model.enums.UserEventType;
import ru.lottery.service.UserEventRecorder;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.type.MutableArgumentValue;
import lombok.RequiredArgsConstructor;

@Singleton
@InterceptorBean(RecordUserEvent.class)
@RequiredArgsConstructor
public class RecordUserEventInterceptor implements MethodInterceptor<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(RecordUserEventInterceptor.class);

    private final UserEventRecorder userEventRecorder;

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> ctx) {
        Object result = ctx.proceed();
        try {
            AnnotationValue<RecordUserEvent> annotation = ctx.getAnnotation(RecordUserEvent.class);
            if (annotation == null) {
                return result;
            }
            UserEventType type = annotation.enumValue("value", UserEventType.class).orElse(null);
            if (type == null) {
                return result;
            }
            UserSource userFrom =
                    annotation.enumValue("userFrom", UserSource.class).orElse(UserSource.RETURN_VALUE);
            String userArg = annotation.stringValue("userArg").orElse("");
            String payloadArg = annotation.stringValue("payloadArg").orElse("");

            Map<String, MutableArgumentValue<?>> parameters = ctx.getParameters();
            Object explicitPayload = readArg(parameters, payloadArg);

            switch (userFrom) {
                case RETURN_VALUE -> {
                    if (result instanceof User user) {
                        recordForUser(user, type, explicitPayload);
                    } else {
                        LOG.warn(
                                "@RecordUserEvent({}) on {}: return value is not a User, skipping",
                                type,
                                ctx.getExecutableMethod());
                    }
                }
                case NAMED_USER_ARG -> {
                    Object value = readArg(parameters, userArg);
                    if (value instanceof User user) {
                        recordForUser(user, type, explicitPayload);
                    } else {
                        LOG.warn(
                                "@RecordUserEvent({}) on {}: arg '{}' is not a User, skipping",
                                type,
                                ctx.getExecutableMethod(),
                                userArg);
                    }
                }
                case NAMED_PRINCIPAL_ARG -> {
                    Object value = readArg(parameters, userArg);
                    if (value instanceof Principal principal) {
                        recordByEmail(principal.getName(), type, explicitPayload);
                    } else {
                        LOG.warn(
                                "@RecordUserEvent({}) on {}: arg '{}' is not a Principal, skipping",
                                type,
                                ctx.getExecutableMethod(),
                                userArg);
                    }
                }
                case NAMED_EMAIL_ARG -> {
                    Object value = readArg(parameters, userArg);
                    if (value instanceof String email) {
                        recordByEmail(email, type, explicitPayload);
                    } else {
                        LOG.warn(
                                "@RecordUserEvent({}) on {}: arg '{}' is not a String email, skipping",
                                type,
                                ctx.getExecutableMethod(),
                                userArg);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to record user event for {}", ctx.getExecutableMethod(), e);
        }
        return result;
    }

    private void recordForUser(User user, UserEventType type, Object explicitPayload) {
        Object payload = explicitPayload != null ? explicitPayload : Map.of("email", user.getEmail());
        userEventRecorder.record(user, type, payload);
    }

    private void recordByEmail(String email, UserEventType type, Object explicitPayload) {
        Object payload = explicitPayload != null ? explicitPayload : Map.of("email", email);
        userEventRecorder.recordByEmail(email, type, payload);
    }

    private static Object readArg(Map<String, MutableArgumentValue<?>> parameters, String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        MutableArgumentValue<?> arg = parameters.get(name);
        return arg == null ? null : arg.getValue();
    }
}