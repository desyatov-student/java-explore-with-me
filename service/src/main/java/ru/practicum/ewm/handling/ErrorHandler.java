package ru.practicum.ewm.handling;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.ewm.exception.ConditionsNotMetException;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;

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

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicatedData(final DuplicatedDataException e) {
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException e) {
        log.error("Bad Request", e);
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final MissingRequestHeaderException e) {
        log.error("Bad Request", e);
        return new ErrorResponse(
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConditionsNotMetException(final ConditionsNotMetException e) {
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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ValidationErrorResponse onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ValidationErrorResponse error = new ValidationErrorResponse("Ошибка валидации MethodArgument");
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            error.getViolations().add(
                    new Violation(fieldError.getField(), fieldError.getDefaultMessage())
            );
        }
        log.error("Validation errors {} ", error, e);
        return error;
    }
}