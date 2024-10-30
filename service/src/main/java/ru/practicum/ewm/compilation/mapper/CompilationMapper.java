package ru.practicum.ewm.compilation.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.mapper.EventMapperImpl;
import ru.practicum.ewm.event.model.Event;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, uses = {EventMapperImpl.class})
public interface CompilationMapper {

    CompilationDto toDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    Compilation toModel(NewCompilationDto request, List<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Compilation updateModel(@MappingTarget Compilation compilation, UpdateCompilationRequest request, List<Event> events);
}
