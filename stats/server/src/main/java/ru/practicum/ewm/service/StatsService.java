package ru.practicum.ewm.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.GetStatsRequest;
import ru.practicum.ewm.dto.NewEndpointHitRequestDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.mapper.EndpointHitMapper;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.model.ViewStats;
import ru.practicum.ewm.repository.HitRepository;
import ru.practicum.ewm.utils.DateMapper;

import java.util.List;
import java.util.function.Function;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final HitRepository hitRepository;
    private final EndpointHitMapper endpointHitMapper;
    private final DateMapper dateMapper;

    @Transactional
    public EndpointHitDto create(NewEndpointHitRequestDto request) {
        EndpointHit hit = endpointHitMapper.toHit(request);
        hit.setTimestamp(dateMapper.now());
        hit = hitRepository.save(hit);
        return endpointHitMapper.toDto(hit);
    }

    public List<ViewStatsDto> getStats(GetStatsRequest request) {
        Function<GetStatsRequest, List<ViewStats>> countHits;
        if (request.getUnique()) {
            if (request.getUris().isEmpty()) {
                countHits = (r) -> hitRepository.countHitsByAppAndUriAndUniqueIp(r.getStart(), r.getEnd());
            } else  {
                countHits = (r) -> hitRepository.countHitsByAppAndUriAndUniqueIp(r.getStart(), r.getEnd(), r.getUris());
            }
        } else  {
            if (request.getUris().isEmpty()) {
                countHits = (r) -> hitRepository.countHitsByAppAndUri(r.getStart(), r.getEnd());
            } else {
                countHits = (r) -> hitRepository.countHitsByAppAndUri(r.getStart(), r.getEnd(), r.getUris());
            }
        }
        return countHits.apply(request).stream()
                .map(endpointHitMapper::toDto)
                .toList();
    }
}
