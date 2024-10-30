package ru.practicum.ewm.handling;

import lombok.Data;

import java.time.Instant;

@Data
public class ApiError {
    private String status;
    private String reason;
    private String message;
    private Instant timestamp = Instant.now();

    public ApiError(String status, String reason, String message) {
        this.status = status;
        this.reason = reason;
        this.message = message;
    }

    static ApiError conflict(String message) {
        return new ApiError(
                "FORBIDDEN",
                "For the requested operation the conditions are not met.",
                message
        );
    }

    static ApiError badRequest(String message) {
        return new ApiError(
                "BAD_REQUEST",
                "Incorrectly made request.",
                message
        );
    }

    static ApiError notFound(String message) {
        return new ApiError(
                "NOT_FOUND",
                "The required object was not found.",
                message
        );
    }
}
