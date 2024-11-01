package ru.practicum.ewm.web.publicapi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.GetCategoriesRequest;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.GetCommentsRequest;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.GetCompilationsRequest;
import ru.practicum.ewm.compilation.service.CompilationService;
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

    private static final String APP_NAME = "ewm-main-service";

    private final CategoryService categoryService;
    private final EventService eventService;
    private final StatsClient statsClient;
    private final CompilationService compilationService;
    private final CommentService commentService;

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        GetCategoriesRequest request = new GetCategoriesRequest(from, size);
        return categoryService.getCategories(request);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategoryById(@PathVariable Long catId) {
        return categoryService.getById(catId);
    }

    // События

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
                    APP_NAME,
                    request.getRequestURI(),
                    request.getRemoteAddr()
            );
            statsClient.create(newEndpointHit);
        } catch (Exception e) {
            log.error("Send stats error", e);
        }
    }

    // Подборки

    @GetMapping("/compilations")
    public List<CompilationDto> getCompilations(
            @RequestParam(defaultValue = "false") Boolean pinned,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        GetCompilationsRequest getEventsRequest = new GetCompilationsRequest(pinned, from, size);
        return compilationService.getCompilations(getEventsRequest);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilationById(@PathVariable @Positive Long compId) {
        return compilationService.getById(compId);
    }

    // Комментарии

    @GetMapping("/comments")
    public List<CommentDto> getEventComments(
            @RequestParam @Positive Long eventId,
            @Min(value = 0) @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        return commentService.getEventComments(eventId,  new GetCommentsRequest(from, size));
    }
}