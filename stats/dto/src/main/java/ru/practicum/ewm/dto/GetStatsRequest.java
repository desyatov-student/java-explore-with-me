package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@AllArgsConstructor
public class GetStatsRequest {
    private Instant start;
    private Instant end;
    private Set<String> uris;
    private Boolean unique;
}
