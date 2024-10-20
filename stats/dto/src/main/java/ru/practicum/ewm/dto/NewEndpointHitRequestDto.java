package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewEndpointHitRequestDto {
    @NotBlank
    private String app;
    @NotBlank
    private String uri;
    @NotBlank
    private String ip;
}
