package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.GetCompilationsRequest;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper mapper;
    private final EventRepository eventRepository;

    public List<CompilationDto> getCompilations(GetCompilationsRequest request) {
        Integer from = request.getFrom();
        Integer size = request.getSize();
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        return compilationRepository.findAll(page).stream()
                .map(mapper::toDto)
                .toList();
    }

    public CompilationDto getById(Long compId) {
        return mapper.toDto(getCompilationById(compId));
    }

    public Compilation getCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Compilation not found with id = %d", compId);
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
    }

    @Transactional
    public CompilationDto create(NewCompilationDto request) {
        List<Event> events = List.of();
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            events = eventRepository.findByIdIn(request.getEvents());
        }
        Compilation compilation = mapper.toModel(request, events);
        compilation = compilationRepository.save(compilation);
        log.info("Creating compilation is successful: {}", compilation);
        return mapper.toDto(compilation);
    }

    @Transactional
    public void removeCompilation(Long compId) {
        Compilation compilation = getCompilationById(compId);
        compilationRepository.delete(compilation);
    }

    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        List<Event> events = List.of();
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            events = eventRepository.findByIdIn(request.getEvents());
        }
        Compilation compilation = getCompilationById(compId);
        compilation = mapper.updateModel(compilation, request, events);
        compilation = compilationRepository.save(compilation);
        return mapper.toDto(compilation);
    }
}
