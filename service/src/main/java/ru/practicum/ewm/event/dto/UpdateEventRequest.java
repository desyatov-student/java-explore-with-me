package ru.practicum.ewm.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.validation.FutureAfterHours;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventRequest {
    private String annotation;
    private String title;
    private String description;
    @FutureAfterHours(hours = 2)
    private String eventDate;
    private Long category;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private EventUserStateAction stateAction;
}
