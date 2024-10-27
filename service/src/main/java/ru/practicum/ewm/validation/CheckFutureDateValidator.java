package ru.practicum.ewm.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.ewm.utils.DateMapper;

import java.time.LocalDateTime;

public class CheckFutureDateValidator implements ConstraintValidator<FutureAfterHours, String> {

    private int hours;

    @Override
    public void initialize(FutureAfterHours constraintAnnotation) {
        this.hours = constraintAnnotation.hours();
    }

    @Override
    public boolean isValid(String dateString, ConstraintValidatorContext constraintValidatorContext) {
        DateMapper mapper = new DateMapper();
        LocalDateTime start = mapper.toLocalDateTime(dateString);
        if (start == null) {
            return true;
        }
        LocalDateTime current = mapper.nowLocalDateTime().plusHours(hours);
        return !current.isAfter(start);
    }
}
