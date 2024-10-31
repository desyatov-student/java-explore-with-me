package ru.practicum.ewm.event.dto;

import lombok.Data;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.utils.DateMapper;

import java.time.LocalDateTime;
import java.util.Set;

import static ru.practicum.ewm.event.dto.EventSort.VIEWS;

@Data
public class GetEventsRequest {
    private String text;
    private Set<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private EventSort sort;
    private Integer from;
    private Integer size;
    private Set<EventState> states;
    private Set<Long> users;

    public GetEventsRequest(String text, Set<Long> categories, Boolean paid, String rangeStart,
                            String rangeEnd, Boolean onlyAvailable, EventSort sort, Integer from,
                            Integer size, Set<EventState> states, Set<Long> users) {

        DateMapper dateMapper = new DateMapper();
        LocalDateTime startDate = null;
        if (rangeStart != null) {
            startDate = dateMapper.toLocalDateTime(rangeStart);
        }

        LocalDateTime endDate = null;
        if (rangeStart != null) {
            endDate = dateMapper.toLocalDateTime(rangeEnd);
        }

        if (startDate != null && endDate != null && !startDate.isBefore(endDate)) {
            String message = String.format("RangeStart is not before rangeEnd. Start: %s end: %s", rangeStart, rangeEnd);
            throw new ValidationException(message);
        }

        this.text = text;
        this.categories = categories;
        this.paid = paid;
        this.rangeStart = startDate;
        this.rangeEnd = endDate;
        this.onlyAvailable = onlyAvailable;
        this.sort = sort;
        this.from = from;
        this.size = size;
        this.states = states;
        this.users = users;
    }

    public boolean isSortByViews() {
        return this.getSort() != null && this.getSort().equals(VIEWS);
    }
}