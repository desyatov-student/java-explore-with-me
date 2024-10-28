package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.validation.FutureAfterHours;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;
    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;
    @NotNull
    @FutureAfterHours(hours = 2)
    private String eventDate;
    @NotNull
    @Positive
    private Long category;
    @NotNull
    private Location location;
    private Boolean paid = false;
    @Min(value = 0)
    private Integer participantLimit = 0;
    private Boolean requestModeration = true;
}