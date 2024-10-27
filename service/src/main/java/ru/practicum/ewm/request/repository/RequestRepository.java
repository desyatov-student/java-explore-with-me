package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.dto.RequestCount;
import ru.practicum.ewm.request.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("SELECT new ru.practicum.ewm.request.dto.RequestCount(r.event.id, count(r.id)) " +
            "FROM Request r WHERE r.event IN :events " +
            "AND r.status IN :statuses " +
            "GROUP BY r.event.id")
    List<RequestCount> countConfirmedRequestByEvents(
            @Param("events") List<Event> events,
            @Param("statuses") List<RequestStatus> statuses
    );
}