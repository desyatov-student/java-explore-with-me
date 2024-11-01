package ru.practicum.ewm.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetCommentsRequest {
    private Integer from;
    private Integer size;
}
