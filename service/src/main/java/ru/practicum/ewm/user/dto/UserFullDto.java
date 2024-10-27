package ru.practicum.ewm.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserFullDto {
    private Long id;
    private String email;
    private String name;
}
