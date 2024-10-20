package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.NewEndpointHitRequestDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.model.ViewStats;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface EndpointHitMapper {

    EndpointHitDto toDto(EndpointHit hit);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    EndpointHit toHit(NewEndpointHitRequestDto request);

    ViewStatsDto toDto(ViewStats viewStats);
}
