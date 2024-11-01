package ru.practicum.ewm.web.privateapi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.GetCommentsRequest;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.GetEventsByInitiatorRequest;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.service.RequestService;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class PrivateController {

    private final EventService eventService;
    private final RequestService requestService;
    private final CommentService commentService;

    // Private: События

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getEvents(
            @PathVariable @Positive Long userId,
            @Min(value = 0) @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        return eventService.getEventsByInitiatorId(new GetEventsByInitiatorRequest(userId, from, size));
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getEventByIdAndInitiatorId(
            @PathVariable @Positive Long eventId,
            @PathVariable @Positive Long userId
    ) {
        return eventService.getByIdAndInitiatorId(eventId, userId);
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody NewEventDto request
    ) {
        return eventService.create(userId, request);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateEventByIdAndInitiatorId(
            @PathVariable @Positive Long eventId,
            @PathVariable @Positive Long userId,
            @Valid @RequestBody UpdateEventUserRequest request
    ) {
        return eventService.updateEventByIdAndInitiatorId(eventId, userId, request);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<RequestDto> getCurrentUserEventRequests(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId
    ) {
        return requestService.getCurrentUserEventRequests(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestsStatus(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest request
    ) {
        return requestService.updateRequestsStatus(userId, eventId, request);
    }

    // Запросы

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createRequest(
            @PathVariable @Positive Long userId,
            @RequestParam @Positive Long eventId
    ) {
        return requestService.create(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public RequestDto cancelRequest(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long requestId
    ) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/users/{userId}/requests")
    public List<RequestDto> getRequests(@PathVariable @Positive Long userId) {
        return requestService.getRequestsInOtherEventsByUserId(userId);
    }

    // Комментарии

    @GetMapping("/users/{userId}/comments")
    public List<CommentDto> getAuthorComments(
            @PathVariable @Positive Long userId,
            @Min(value = 0) @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        return commentService.getAuthorComments(userId, new GetCommentsRequest(from, size));
    }

    @PostMapping("/users/{userId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable @Positive Long userId,
            @RequestParam @Positive Long eventId,
            @Valid @RequestBody NewCommentDto request
    ) {
        return commentService.create(userId, eventId, request);
    }
}