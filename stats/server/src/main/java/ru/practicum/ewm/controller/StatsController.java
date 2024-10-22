package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.GetStatsRequest;
import ru.practicum.ewm.dto.NewEndpointHitRequestDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.service.StatsService;
import ru.practicum.ewm.utils.DateMapper;

import java.util.List;
import java.util.Set;


@RestController
@RequestMapping(path = "/")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final DateMapper dateMapper;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("hit")
    public EndpointHitDto create(
            @Valid @RequestBody NewEndpointHitRequestDto request
    ) {
        return statsService.create(request);
    }

    @GetMapping("stats")
    public List<ViewStatsDto> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) Set<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique

    ) {
        Set<String> urisResult = (uris == null) ? Set.of() : uris;
        GetStatsRequest request = new GetStatsRequest(
                dateMapper.toInstant(start),
                dateMapper.toInstant(end),
                urisResult,
                unique
        );
        return statsService.getStats(request);
    }
}