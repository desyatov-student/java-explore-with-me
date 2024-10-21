package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.GetStatsRequest;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.hit.HitClient;
import ru.practicum.ewm.utils.DateMapper;

import java.util.List;
import java.util.Set;


@RestController
@RequestMapping(path = "/")
@RequiredArgsConstructor
public class ExampleController {

    private final HitClient hitClient;
    private final DateMapper dateMapper;

    @GetMapping("stats")
    public List<ViewStatsDto> getStats() {
        GetStatsRequest request = new GetStatsRequest(
                dateMapper.toInstant("2024-10-20 12:50:00"),
                dateMapper.toInstant("2024-10-20 12:57:00"),
                Set.of("/events"),
                false
        );
        return hitClient.getStats(request);
    }
}