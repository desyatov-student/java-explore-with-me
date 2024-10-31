package ru.practicum.ewm.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetEventsByInitiatorRequest {
    private Long initiatorId;
    private Integer from;
    private Integer size;
}
