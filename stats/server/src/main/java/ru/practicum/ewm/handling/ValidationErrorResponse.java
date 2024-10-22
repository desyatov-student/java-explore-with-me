package ru.practicum.ewm.handling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class ValidationErrorResponse {
    private final String error;
    private final List<Violation> violations = new ArrayList<>();
}
