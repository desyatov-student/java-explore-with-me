package ru.practicum.ewm.web.publicapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import java.util.List;


@RestController
@RequestMapping(path = "/")
@RequiredArgsConstructor
public class PublicController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public List<CategoryDto> getCategories() {
        return categoryService.getCategories();
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getById(@PathVariable Long catId) {
        return categoryService.getById(catId);
    }
}