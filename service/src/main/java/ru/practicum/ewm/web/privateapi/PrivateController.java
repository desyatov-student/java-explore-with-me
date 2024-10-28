package ru.practicum.ewm.web.privateapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class PrivateController {

    private final EventService eventService;

    // Private: События

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getEvents(@PathVariable Long userId) {
        return eventService.getEventsByInitiatorId(userId);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getEventByIdAndInitiatorId(
            @PathVariable Long eventId,
            @PathVariable Long userId
    ) {
        return eventService.getByIdAndInitiatorId(eventId, userId);
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto request
    ) {
        return eventService.create(userId, request);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateEventByIdAndInitiatorId(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateEventUserRequest request
    ) {
        return eventService.updateEventByIdAndInitiatorId(eventId, userId, request);
    }

}