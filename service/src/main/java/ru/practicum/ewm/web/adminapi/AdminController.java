package ru.practicum.ewm.web.adminapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.dto.UpdateCategoryRequest;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.EventSort;
import ru.practicum.ewm.event.dto.GetEventsRequest;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.user.dto.GetUsersRequest;
import ru.practicum.ewm.user.dto.NewUserDto;
import ru.practicum.ewm.user.dto.UserFullDto;
import ru.practicum.ewm.user.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final EventService eventService;

    // Пользователи

    @GetMapping("/users")
    public List<UserFullDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return userService.getUsers(new GetUsersRequest(ids == null ? List.of() : ids, from, size));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserFullDto create(@Valid @RequestBody NewUserDto request) {
        return userService.create(request);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(@PathVariable Long userId) {
        userService.removeUser(userId);
    }

    // Категории

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody NewCategoryDto request) {
        return categoryService.create(request);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCategory(@PathVariable Long catId) {
        categoryService.removeCategory(catId);
    }

    @PatchMapping("/categories/{catId}")
    public CategoryDto update(
            @PathVariable Long catId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return categoryService.update(catId, request);
    }

    // События

    @GetMapping("/events")
    public List<EventFullDto> getEvents(
            @RequestParam(defaultValue = "") Set<Long> users,
            @RequestParam(name = "states", defaultValue = "") Set<String> statesParam,
            @RequestParam(defaultValue = "") Set<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {

        Set<EventState> states = statesParam.stream().map(state -> EventState.from(state)
                    .orElseThrow(() -> new ValidationException("Unknown state: " + state))
                ).collect(Collectors.toSet());

        GetEventsRequest getEventsRequest = new GetEventsRequest(
                null,
                categories,
                null,
                rangeStart,
                rangeEnd,
                null,
                null,
                from,
                size,
                states,
                users
        );
        return eventService.getFullEvents(getEventsRequest);
    }
}