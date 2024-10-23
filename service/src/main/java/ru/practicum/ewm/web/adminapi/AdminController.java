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
import ru.practicum.ewm.category.dto.NewCategoryRequest;
import ru.practicum.ewm.category.dto.UpdateCategoryRequest;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.user.dto.GetUsersRequest;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

import java.util.List;


@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CategoryService categoryService;

    @GetMapping("/users")
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return userService.getUsers(new GetUsersRequest(ids == null ? List.of() : ids, from, size));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody NewUserRequest request) {
        return userService.create(request);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(@PathVariable Long userId) {
        userService.removeUser(userId);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody NewCategoryRequest request) {
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
}