package ru.practicum.ewm.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.utils.DateMapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, uses = { DateMapper.class })
public interface RequestMapper {

    @Mapping(source = "request.event.id", target = "event")
    @Mapping(source = "request.requester.id", target = "requester")
    RequestDto toDto(Request request);
}
