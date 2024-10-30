package ru.practicum.ewm.web.adminapi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.dto.UpdateCategoryRequest;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.GetEventsRequest;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
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
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;

    // Пользователи

    @GetMapping("/admin/users")
    public List<UserFullDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return userService.getUsers(new GetUsersRequest(ids == null ? List.of() : ids, from, size));
    }

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserFullDto createUser(@Valid @RequestBody NewUserDto request) {
        return userService.create(request);
    }

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(@PathVariable Long userId) {
        userService.removeUser(userId);
    }

    // Категории

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto request) {
        return categoryService.create(request);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCategory(@PathVariable Long catId) {
        categoryService.removeCategory(catId);
    }

    @PatchMapping("/admin/categories/{catId}")
    public CategoryDto updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return categoryService.update(catId, request);
    }

    // События

    @GetMapping("/admin/events")
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

        GetEventsRequest getEventsRequest = new GetEventsRequest(null, categories, null, rangeStart, rangeEnd,
                null, null, from, size, states, users
        );
        return eventService.getFullEvents(getEventsRequest);
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest request
    ) {
        return eventService.updateEventById(eventId, request);
    }

    // Подборки

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admin/compilations")
    public CompilationDto createCompilation(@Valid @RequestBody NewCompilationDto request) {
        return compilationService.create(request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/compilations/{compId}")
    public void deleteCompilation(@PathVariable @Positive Long compId) {
        compilationService.removeCompilation(compId);
    }

    @PatchMapping("/admin/compilations/{compId}")
    public CompilationDto updateCompilation(
            @PathVariable @Positive Long compId,
            @Valid @RequestBody UpdateCompilationRequest request
    ) {
        return compilationService.update(compId, request);
    }
}