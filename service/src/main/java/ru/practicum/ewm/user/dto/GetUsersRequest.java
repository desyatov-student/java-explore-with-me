package ru.practicum.ewm.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GetUsersRequest {
    private List<Long> ids;
    private Integer from;
    private Integer size;
}
