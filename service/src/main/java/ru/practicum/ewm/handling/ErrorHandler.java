package ru.practicum.ewm.handling;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.ewm.exception.ConditionsNotMetException;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.validation.FutureAfterHours;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final NotFoundException e) {
        log.error("Not found error", e);
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({ForbiddenException.class, DuplicatedDataException.class})
    public ErrorResponse handleDuplicatedData(final Exception e) {
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ExceptionHandler({MissingRequestHeaderException.class, ConditionsNotMetException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConditionsNotMetException(final Exception e) {
        log.error("Bad Request", e);
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        log.error("Argument mismatch error", e);
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ValidationErrorResponse onConstraintValidationException(ConstraintViolationException e) {
        ValidationErrorResponse error = new ValidationErrorResponse("Ошибка валидации Constraint");
        for (ConstraintViolation violation : e.getConstraintViolations()) {
            error.getViolations().add(
                    new Violation(violation.getPropertyPath().toString(), violation.getMessage())
            );
        }
        log.error("Validation errors {} ", error, e);
        return error;
    }

    @ExceptionHandler
    ResponseEntity<ValidationErrorResponse> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ValidationErrorResponse error = new ValidationErrorResponse("Ошибка валидации MethodArgument");
        List<Violation> violations = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            Violation violation = new Violation(fieldError.getField(), fieldError.getDefaultMessage());
            // Для ошибки проверки даты начала. В ТЗ говорится про 409. в тестах 400. Можно поменять тут
            if (fieldError.getCode() != null && fieldError.getCode().equals(FutureAfterHours.class.getSimpleName())) {
                error.getViolations().add(violation);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            violations.add(violation);
        }
        error.getViolations().addAll(violations);
        log.error("Validation errors {} ", error, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler
    ResponseEntity<ValidationErrorResponse> onHandlerMethodValidationException(HandlerMethodValidationException e) {
        ValidationErrorResponse error = new ValidationErrorResponse("Ошибка валидации MethodArgument");
        List<Violation> violations = new ArrayList<>();
    //    for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
    //        Violation violation = new Violation(fieldError.getField(), fieldError.getDefaultMessage());
    //        // Для ошибки проверки даты начала. В ТЗ говорится про 409. в тестах 400. Можно поменять тут
    //        if (fieldError.getCode() != null && fieldError.getCode().equals(FutureAfterHours.class.getSimpleName())) {
    //            error.getViolations().add(violation);
    //            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    //        }
    //        violations.add(violation);
    //    }
        error.getViolations().addAll(violations);
        log.error("Validation errors {} ", error, e);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
