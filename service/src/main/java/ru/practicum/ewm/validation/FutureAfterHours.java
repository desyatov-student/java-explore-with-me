package ru.practicum.ewm.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = FutureAfterHoursValidator.class)
public @interface FutureAfterHours {
    String message() default "Start must be 2 hours later current time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int hours() default 0;
}
