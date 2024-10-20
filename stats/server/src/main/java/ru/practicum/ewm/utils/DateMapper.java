package ru.practicum.ewm.utils;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateMapper {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZONE);

    public String toString(Instant date) {
        return date != null ? formatter.format(date) : null;
    }

    public Instant toInstant(String stringDate) {
        if (stringDate == null) {
            return null;
        }
        ZonedDateTime localDateTime = LocalDateTime.parse(stringDate, formatter).atZone(ZONE);
        return localDateTime.toInstant();
    }

    public LocalDateTime toLocalDateTime(String stringDate) {
        if (stringDate == null) {
            return null;
        }
        ZonedDateTime localDateTime = LocalDateTime.parse(stringDate, formatter).atZone(ZONE);
        return localDateTime.toLocalDateTime();
    }

    public Instant toInstant(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return date.atZone(ZONE).toInstant();
    }

    public static Instant now() {
        return LocalDateTime.now().atZone(ZONE).toInstant();
    }
}