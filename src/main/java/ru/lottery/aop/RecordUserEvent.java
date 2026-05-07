package ru.lottery.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ru.lottery.model.enums.UserEventType;

import io.micronaut.aop.Around;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Around
public @interface RecordUserEvent {

  UserEventType value();

  UserSource userFrom() default UserSource.RETURN_VALUE;

  String userArg() default "";

  String payloadArg() default "";
}
