package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.model.ViewStats;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface HitRepository extends JpaRepository<EndpointHit, Long> {
    @Query("select new ru.practicum.ewm.model.ViewStats(h.app, h.uri, count(h.id)) " +
            "from EndpointHit as h " +
            "where 1=1 " +
            "and h.timestamp >= ?1 " +
            "and h.timestamp < ?2 " +
            "group by h.app, h.uri " +
            "order by count(h.id) desc")
    List<ViewStats> countHitsByAppAndUri(Instant start, Instant end);

    @Query("select new ru.practicum.ewm.model.ViewStats(h.app, h.uri, count(h.id)) " +
            "from EndpointHit as h " +
            "where 1=1 " +
            "and h.uri in (?3) " +
            "and h.timestamp >= ?1 " +
            "and h.timestamp < ?2 " +
            "group by h.app, h.uri " +
            "order by count(h.id) desc")
    List<ViewStats> countHitsByAppAndUri(Instant start, Instant end, Set<String> uris);

    @Query("select new ru.practicum.ewm.model.ViewStats(h.app, h.uri, count(distinct h.ip)) " +
            "from EndpointHit as h " +
            "where 1=1 " +
            "and h.timestamp >= ?1 " +
            "and h.timestamp < ?2 " +
            "group by h.app, h.uri " +
            "order by count(h.id) desc")
    List<ViewStats> countHitsByAppAndUriAndUniqueIp(Instant start, Instant end);

    @Query("select new ru.practicum.ewm.model.ViewStats(h.app, h.uri, count(distinct h.ip)) " +
            "from EndpointHit as h " +
            "where 1=1 " +
            "and h.uri in (?3) " +
            "and h.timestamp >= ?1 " +
            "and h.timestamp < ?2 " +
            "group by h.app, h.uri " +
            "order by count(h.id) desc")
    List<ViewStats> countHitsByAppAndUriAndUniqueIp(Instant start, Instant end, Set<String> uris);
}