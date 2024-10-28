package ru.practicum.ewm.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.ewm.event.model.EventState;

import java.util.Set;

import static ru.practicum.ewm.event.dto.EventSort.VIEWS;

@Data
@AllArgsConstructor
public class GetEventsRequest {
    private String text;
    private Set<Long> categories;
    private Boolean paid;
    private String rangeStart;
    private String rangeEnd;
    private Boolean onlyAvailable;
    private EventSort sort;
    private Integer from;
    private Integer size;
    private Set<EventState> states;
    private Set<Long> users;

    public boolean isSortByViews() {
        return this.getSort() != null && this.getSort().equals(VIEWS);
    }
}