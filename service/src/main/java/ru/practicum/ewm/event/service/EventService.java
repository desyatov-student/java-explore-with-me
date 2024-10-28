package ru.practicum.ewm.event.service;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.dto.GetStatsRequest;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.GetEventsRequest;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.RequestCount;
import ru.practicum.ewm.request.model.QRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.ewm.utils.DateMapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.ewm.event.dto.EventSort.EVENT_DATE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final EventMapper mapper;
    private final UserService userService;
    private final CategoryService categoryService;
    private final DateMapper dateMapper;
    private final StatsClient statsClient;

    @PersistenceContext
    private final EntityManager entityManager;

    public List<EventShortDto> getShortEvents(GetEventsRequest params) {
        List<Event> events = getEvents(params);
        return enrichedShortEventsWithAdditionalData(events, params.isSortByViews());
    }

    public List<EventFullDto> getFullEvents(GetEventsRequest params) {
        List<Event> events = getEvents(params);
        return enrichedFullEventsWithAdditionalData(events, params.isSortByViews());
    }

    private List<Event> getEvents(GetEventsRequest params) {
        QRequest request = QRequest.request;
        JPAQuery<Event> query = new JPAQuery<>(entityManager);
        QEvent event = QEvent.event;

        query.from(event);

        BooleanExpression predicate = Expressions.TRUE;
        if (!params.getStates().isEmpty()) {
            predicate = predicate.and(QEvent.event.state.in(params.getStates()));
        }

        if (!params.getCategories().isEmpty()) {
            predicate = predicate.and(QEvent.event.category.id.in(params.getCategories()));
        }

        if (!params.getUsers().isEmpty()) {
            predicate = predicate.and(QEvent.event.initiator.id.in(params.getUsers()));
        }

        if (params.getText() != null && !params.getText().isBlank()) {
            BooleanExpression text = Expressions.anyOf(
                    QEvent.event.annotation.containsIgnoreCase(params.getText()),
                    QEvent.event.description.containsIgnoreCase(params.getText())
            );
            predicate = predicate.and(text);
        }

        if (params.getPaid() != null && params.getPaid()) {
            predicate = predicate.and(event.paid.eq(params.getPaid()));
        }

        if (params.getRangeStart() != null) {
            Instant rangeStart = dateMapper.toInstant(params.getRangeStart());
            BooleanExpression rangeStartExp = QEvent.event.eventDate.before(Expressions.asDate(rangeStart)).not();
            predicate = predicate.and(rangeStartExp);
        }
        if (params.getRangeEnd() != null) {
            Instant rangeEnd = dateMapper.toInstant(params.getRangeEnd());
            BooleanExpression rangeEndExp = QEvent.event.eventDate.before(Expressions.asDate(rangeEnd));
            predicate = predicate.and(rangeEndExp);
        }

        if (params.getRangeStart() == null && params.getRangeEnd() == null) {
            Instant rangeStart = dateMapper.now();
            BooleanExpression rangeStartExp = QEvent.event.eventDate.after(Expressions.asDate(rangeStart));
            predicate = predicate.and(rangeStartExp);
        }

        if (params.getOnlyAvailable() != null && params.getOnlyAvailable()) {
            query.leftJoin(event.requests, request)
                    .on(request.status.in(List.of(RequestStatus.CONFIRMED)))
                    .groupBy(event.id)
                    .having(request.count().lt(event.participantLimit));
        }

        query.where(predicate);

        if (params.getSort() != null && params.getSort().equals(EVENT_DATE)) {
            OrderSpecifier<Instant> orderSpecifier = event.eventDate.asc();
            query.orderBy(orderSpecifier);
        }

        query.limit(params.getSize())
                .offset(params.getFrom());

        return query.fetch();
    }

    public List<EventShortDto> getEventsByInitiatorId(Long initiatorId) {
        User initiator = getUserById(initiatorId);
        return enrichedShortEventsWithAdditionalData(eventRepository.findByInitiator(initiator), false);
    }

    public EventFullDto getByIdAndInitiatorId(Long eventId, Long initiatorId) {
        Event event = getEventByIdAndInitiatorId(eventId, initiatorId);
        return enrichedFullEventsWithAdditionalData(List.of(event), false).get(0);
    }

    public EventFullDto getByIdAndStates(Long eventId, List<EventState> states) {
        Event event = eventRepository.findByIdAndStateIn(eventId, states)
                .orElseThrow(() -> throwEventNotFound(eventId, null));
        return enrichedFullEventsWithAdditionalData(List.of(event), false).get(0);
    }

    @Transactional
    public EventFullDto create(Long userId, NewEventDto request) {
        User initiator = getUserById(userId);
        Category category = getCategoryById(request.getCategory());
        Event event = mapper.toModel(request, category, initiator);
        event.setCreatedOn(dateMapper.now());
        event.setState(EventState.PENDING);
        event = eventRepository.save(event);
        log.info("Creating event is successful: {}", event);
        EventFullDto eventFullDto = mapper.toFullDto(event);
        eventFullDto.setConfirmedRequests(0L);
        eventFullDto.setViews(0L);
        return eventFullDto;
    }

    @Transactional
    public EventFullDto updateEventByIdAndInitiatorId(Long eventId, Long initiatorId, UpdateEventUserRequest request) {
        Event event = getEventByIdAndInitiatorId(eventId, initiatorId);
        List<EventState> allowedStates = List.of(EventState.PENDING, EventState.CANCELED);
        if (allowedStates.stream().noneMatch(event.getState()::equals)) {
            String errorMessage = String.format("Only pending or canceled events can be changed, current=%s", event.getState());
            log.error(errorMessage);
            throw new ForbiddenException(errorMessage);
        }
        event = mapper.updateModel(event, request);
        EventState newState = switch (request.getStateAction()) {
            case SEND_TO_REVIEW -> EventState.PENDING;
            case CANCEL_REVIEW -> EventState.CANCELED;
        };
        event.setState(newState);
        if (request.getCategory() != null) {
            Category category = getCategoryById(request.getCategory());
            event.setCategory(category);
        }
        event = eventRepository.save(event);
        return enrichedFullEventsWithAdditionalData(List.of(event), false).get(0);
    }

    @Transactional
    public EventFullDto updateEventById(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEventById(eventId);
        EventState newState = null;
        switch (request.getStateAction()) {
            case PUBLISH_EVENT -> {
                if (!event.getState().equals(EventState.PENDING)) {
                    String errorMessage = "Cannot publish the event because it's not in the right state: PUBLISHED, current: " + event.getState();
                    log.error(errorMessage);
                    throw new ForbiddenException(errorMessage);
                }
                newState = EventState.PUBLISHED;
            }
            case REJECT_EVENT -> {
                if (event.getState().equals(EventState.PUBLISHED)) {
                    String errorMessage = "Cannot publish the event because it's not in the right state: CANCELED, PENDING, current: " + event.getState();
                    log.error(errorMessage);
                    throw new ForbiddenException(errorMessage);
                }
                newState = EventState.CANCELED;
            }
        }

        event = mapper.updateModel(event, request);
        event.setState(newState);
        if (request.getCategory() != null) {
            Category category = getCategoryById(request.getCategory());
            event.setCategory(category);
        }
        event = eventRepository.save(event);
        return enrichedFullEventsWithAdditionalData(List.of(event), false).get(0);
    }

    // Запрос подтвержденных запросов и просмотров
    private List<EventShortDto> enrichedShortEventsWithAdditionalData(List<Event> events, boolean sortByViews) {
        if (events.isEmpty()) {
            return List.of();
        }
        List<RequestStatus> statuses = List.of(RequestStatus.CONFIRMED);
        Map<Long, Long> confirmedRequests = getCountConfirmedRequestByEvents(events, statuses);
        Map<Long, Long> viewsByEvents = getCountViewsByEvents(events);

        Stream<EventShortDto> stream = events.stream()
                .map(e -> {
                    EventShortDto eventShortDto = mapper.toShortDto(e);
                    eventShortDto.setConfirmedRequests(confirmedRequests.getOrDefault(e.getId(), 0L));
                    eventShortDto.setViews(viewsByEvents.getOrDefault(e.getId(), 0L));
                    return eventShortDto;
                });

        if (sortByViews) {
            return stream.sorted(Comparator.comparing(EventShortDto::getViews).reversed()).toList();
        } else {
            return stream.toList();
        }
    }

    // Запрос подтвержденных запросов и просмотров
    private List<EventFullDto> enrichedFullEventsWithAdditionalData(List<Event> events, boolean sortByViews) {
        if (events.isEmpty()) {
            return List.of();
        }
        List<RequestStatus> statuses = List.of(RequestStatus.CONFIRMED);
        Map<Long, Long> confirmedRequests = getCountConfirmedRequestByEvents(events, statuses);
        Map<Long, Long> viewsByEvents = getCountViewsByEvents(events);

        Stream<EventFullDto> stream = events.stream()
                .map(e -> {
                    EventFullDto eventFullDto = mapper.toFullDto(e);
                    eventFullDto.setConfirmedRequests(confirmedRequests.getOrDefault(e.getId(), 0L));
                    eventFullDto.setViews(viewsByEvents.getOrDefault(e.getId(), 0L));
                    return eventFullDto;
                });
        if (sortByViews) {
            return stream.sorted(Comparator.comparing(EventFullDto::getViews).reversed()).toList();
        } else {
            return stream.toList();
        }
    }

    private Map<Long, Long> getCountConfirmedRequestByEvents(List<Event> events, List<RequestStatus> statuses) {
        return requestRepository.countConfirmedRequestByEvents(events, statuses).stream()
                .collect(Collectors.toMap(RequestCount::getEventId, RequestCount::getConfirmedRequests));
    }

    private Map<Long, Long> getCountViewsByEvents(List<Event> events) {
        if (events.isEmpty()) {
            return new HashMap<>();
        }
        Instant minDate = events.get(0).getCreatedOn();
        Set<String> uris = new HashSet<>();
        for (Event event : events) {
            if (event.getEventDate().isBefore(minDate)) {
                minDate = event.getCreatedOn();
            }
            uris.add("/events/" + event.getId());
        }
        try {
            List<ViewStatsDto> stats = statsClient.getStats(new GetStatsRequest(minDate, dateMapper.now(), uris, true));
            return stats.stream().collect(Collectors.toMap(this::getIdFromUri, ViewStatsDto::getHits));
        } catch (Exception e) {
            log.error("Get stats error", e);
            return Map.of();
        }
    }

    private Long getIdFromUri(ViewStatsDto viewStatsDto) {
        Path path = Paths.get(viewStatsDto.getUri());
        return Long.valueOf(path.getFileName().toString());
    }

    private Event getEventByIdAndInitiatorId(Long eventId, Long initiatorId) {
        User initiator = getUserById(initiatorId);
        return eventRepository.findByIdAndInitiator(eventId, initiator)
                .orElseThrow(() -> throwEventNotFound(eventId, initiatorId));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> throwEventNotFound(eventId, null));
    }

    private NotFoundException throwEventNotFound(Long eventId, Long initiatorId) {
        String errorMessage = String.format("Event not found with eventId=%d", eventId);
        if (initiatorId != null) {
            errorMessage = errorMessage + ", initiatorId=" + initiatorId;
        }
        log.error(errorMessage);
        throw new NotFoundException(errorMessage);
    }

    private User getUserById(Long userId) {
        return userService.getUserById(userId);
    }

    private Category getCategoryById(Long categoryId) {
        return categoryService.getCategoryById(categoryId);
    }
}
