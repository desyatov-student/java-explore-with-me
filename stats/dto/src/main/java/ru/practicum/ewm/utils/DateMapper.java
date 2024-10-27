package ru.practicum.ewm.utils;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Component
public class DateMapper {

    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final ZoneId ZONE = TimeZone.getTimeZone("UTC").toZoneId();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN)
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

    public Instant now() {
        return LocalDateTime.now().atZone(ZONE).toInstant();
    }

    public LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now().atZone(ZONE).toLocalDateTime();
    }
}