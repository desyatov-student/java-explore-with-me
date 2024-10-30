package ru.practicum.ewm.request.dto;

import java.util.Optional;

public enum EventRequestStatus {
    CONFIRMED,
    REJECTED;

    public static Optional<EventRequestStatus> from(String stringStatus) {
        for (EventRequestStatus status : values()) {
            if (status.name().equalsIgnoreCase(stringStatus)) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}
