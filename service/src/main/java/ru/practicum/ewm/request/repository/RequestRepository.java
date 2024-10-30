package ru.practicum.ewm.request.repository;

import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.hibernate.type.OrderedSetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.dto.RequestCount;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.user.model.User;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("SELECT new ru.practicum.ewm.request.dto.RequestCount(r.event.id, count(r.id)) " +
            "FROM Request r WHERE r.event IN :events " +
            "AND r.status IN :statuses " +
            "GROUP BY r.event.id")
    List<RequestCount> countRequestByEventsAndStatuses(
            @Param("events") List<Event> events,
            @Param("statuses") List<RequestStatus> statuses
    );

    Optional<Request> findByRequesterAndEvent(User requester, Event event);

    Optional<Request> findByIdAndRequester(Long eventId, User requester);

    List<Request> findByRequesterAndEventInitiatorNot(User requester, User initiator);

    List<Request> findByEvent(Event event);

    List<Request> findByEventAndIdIn(Event event, OrderedHashSet<Long> requestIds);

    Integer countByEventAndStatus(Event event, RequestStatus status);

}