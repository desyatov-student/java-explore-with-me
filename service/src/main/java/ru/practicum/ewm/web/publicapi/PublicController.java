package ru.practicum.ewm.web.publicapi;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.dto.NewEndpointHitRequestDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.EventSort;
import ru.practicum.ewm.event.dto.GetEventsRequest;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PublicController {

    private final CategoryService categoryService;
    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping("/categories")
    public List<CategoryDto> getCategories() {
        return categoryService.getCategories();
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getById(@PathVariable Long catId) {
        return categoryService.getById(catId);
    }

    @GetMapping("/events")
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "") Set<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(name = "sort", required = false) String sortParam,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        EventSort sort = Optional.ofNullable(sortParam).map(s -> EventSort.from(s)
                .orElseThrow(() -> new ValidationException("Unknown sort: " + s))).orElse(null);

        GetEventsRequest getEventsRequest = new GetEventsRequest(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size,
                Set.of(EventState.PUBLISHED),
                Set.of()
        );

        List<EventShortDto> events = eventService.getShortEvents(getEventsRequest);
        sendStats(request);
        return events;
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getPublishedById(@PathVariable Long eventId, HttpServletRequest request) {
        EventFullDto event = eventService.getByIdAndStates(eventId, List.of(EventState.PUBLISHED));
        sendStats(request);
        return event;
    }

    private void sendStats(HttpServletRequest request) {
        try {
            NewEndpointHitRequestDto newEndpointHit = new NewEndpointHitRequestDto(
                    "ewm-main-service",
                    request.getRequestURI(),
                    request.getRemoteAddr()
            );
            statsClient.create(newEndpointHit);
        } catch (Exception e) {
            log.error("Send stats error", e);
        }
    }
}