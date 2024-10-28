package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.validation.FutureAfterHours;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {
    @Size(min = 20, max = 2000)
    private String annotation;
    @Size(min = 3, max = 120)
    private String title;
    @Size(min = 20, max = 7000)
    private String description;
    @FutureAfterHours(hours = 2)
    private String eventDate;
    private Long category;
    private Location location;
    private Boolean paid;
    @Min(value = 0)
    private Integer participantLimit;
    private Boolean requestModeration;
    private EventUserStateAction stateAction;
}
