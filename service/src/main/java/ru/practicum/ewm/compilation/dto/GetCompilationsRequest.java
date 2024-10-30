package ru.practicum.ewm.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetCompilationsRequest {
    private Boolean pinned;
    private Integer from;
    private Integer size;
}
