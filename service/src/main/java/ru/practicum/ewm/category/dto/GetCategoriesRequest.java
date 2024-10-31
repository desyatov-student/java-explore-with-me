package ru.practicum.ewm.category.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetCategoriesRequest {
    private Integer from;
    private Integer size;
}
