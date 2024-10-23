package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.ewm.validation.ValidEmailAndNotBlank;

@Data
@AllArgsConstructor
public class NewUserDto {
    @ValidEmailAndNotBlank
    private String email;
    @NotBlank
    private String name;
}
