package ru.practicum.ewm.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Location {
    private Double lat;
    private Double lon;
}
