package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.UpdateCategoryRequest;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.category.dto.NewCategoryRequest;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public CategoryDto getById(Long categoryId) {
        return mapper.toDto(getCategoryById(categoryId));
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Category not found with id = %d", categoryId);
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
    }

    @Transactional
    public CategoryDto create(NewCategoryRequest request) {
        Optional<Category> alreadyExistCategory = categoryRepository.findByName(request.getName());
        if (alreadyExistCategory.isPresent()) {
            throwDuplicatedDataException(request.getName());
        }

        Category category = mapper.toModel(request);
        category = categoryRepository.save(category);
        log.info("Creating category is successful: {}", category);
        return mapper.toDto(category);
    }

    @Transactional
    public void removeCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        categoryRepository.deleteById(category.getId());
    }

    @Transactional
    public CategoryDto update(Long categoryId, UpdateCategoryRequest request) {
        Category category = getCategoryById(categoryId);
        if (category.getName().equals(request.getName())) {
            throwDuplicatedDataException(request.getName());
        }
        category = mapper.updateModel(category, request);
        category = categoryRepository.save(category);
        return mapper.toDto(category);
    }

    private void throwDuplicatedDataException(String name) {
        String message = String.format("Could not complete operation. Category with name = %s exists", name);
        log.error(message);
        throw new DuplicatedDataException(message);
    }
}