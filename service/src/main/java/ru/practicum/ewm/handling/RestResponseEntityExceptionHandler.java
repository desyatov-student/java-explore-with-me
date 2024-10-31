package ru.practicum.ewm.handling;

import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.validation.FutureAfterHours;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ForbiddenException.class, DuplicatedDataException.class})
    public ResponseEntity<Object> handleDuplicatedData(final Exception e) {
        ApiError apiError = ApiError.conflict(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFound(final Exception e) {
        ApiError apiError = ApiError.notFound(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<Object> handleBadRequest(final Exception e) {
        ApiError apiError = ApiError.badRequest(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiError apiError = ApiError.badRequest(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (MessageSourceResolvable resolvable : ex.getAllErrors()) {
            // Обработка валидации даты события
            if (resolvable.getCodes() != null && Arrays.stream(resolvable.getCodes()).toList().contains(FutureAfterHours.class.getSimpleName())) {
                ApiError apiError = ApiError.conflict(resolvable.getDefaultMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
            } else {
                errors.add(resolvable.getDefaultMessage());
            }
        }
        ApiError apiError = ApiError.badRequest(StringUtils.join(errors));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }
}