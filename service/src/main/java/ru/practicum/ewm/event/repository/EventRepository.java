package ru.practicum.ewm.event.repository;

import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.user.model.User;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    List<Event> findByInitiator(User initiator, PageRequest page);

    List<Event> findByIdIn(OrderedHashSet<Long> eventIds);

    Optional<Event> findByIdAndInitiator(Long eventId, User initiator);

    Optional<Event> findByIdAndStateIn(Long eventId, List<EventState> states);
}