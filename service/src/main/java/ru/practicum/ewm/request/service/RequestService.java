package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.request.dto.EventRequestStatus;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.ewm.utils.DateMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {

    private final RequestRepository requestRepository;
    private final EventService eventService;
    private final UserService userService;
    private final RequestMapper mapper;
    private final DateMapper dateMapper;

    @Transactional
    public RequestDto create(Long userId, Long eventId) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            String message = String.format("Cannot join the event because it's not in the right state. Current: %s", event.getState());
            log.error(message);
            throw new ForbiddenException(message);
        }

        if (event.getInitiator().getId().equals(userId)) {
            String message = String.format("Initiator can't create request, userId: %d", userId);
            log.error(message);
            throw new ForbiddenException(message);
        }

        Optional<Request> alreadyExistRequest = requestRepository.findByRequesterAndEvent(user, event);
        if (alreadyExistRequest.isPresent()) {
            String message = String.format("Request exists, userId: %d, eventId: %d", userId, eventId);
            log.error(message);
            throw new DuplicatedDataException(message);
        }

        Integer confirmedRequests = requestRepository.countByEventAndStatus(event, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throwParticipantLimitException(confirmedRequests, event.getParticipantLimit());
        }

        Request request = new Request();
        request.setCreated(dateMapper.now());
        request.setRequester(user);
        request.setEvent(event);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        request = requestRepository.save(request);
        return mapper.toDto(request);
    }

    @Transactional
    public RequestDto cancelRequest(Long userId, Long requestId) {
        User requester = getUserById(userId);
        Request request = getRequestById(requestId, requester);
        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);
        return mapper.toDto(request);
    }

    public List<RequestDto> getRequestsInOtherEventsByUserId(Long userId) {
        User requester = getUserById(userId);
        return requestRepository.findByRequesterAndEventInitiatorNot(requester, requester).stream()
                .map(mapper::toDto).toList();
    }

    public List<RequestDto> getCurrentUserEventRequests(Long userId, Long eventId) {
        Event event = getEventByInitiatorAndEventId(userId, eventId);
        return requestRepository.findByEvent(event).stream().map(mapper::toDto).toList();
    }

    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        EventRequestStatus newStatus = EventRequestStatus.from(updateRequest.getStatus())
                .orElseThrow(() -> new ValidationException("Unknown status: " + updateRequest.getStatus()));

        Event event = getEventByInitiatorAndEventId(userId, eventId);
        List<Request> requests = requestRepository.findByEventAndIdIn(event, updateRequest.getRequestIds());
        if (requests.isEmpty()) {
            return EventRequestStatusUpdateResult.empty();
        }

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return new EventRequestStatusUpdateResult(
                    requests.stream().map(mapper::toDto).toList(),
                    List.of()
            );
        }

        Integer confirmedRequestsCount = requestRepository.countByEventAndStatus(event, RequestStatus.CONFIRMED);
        if (confirmedRequestsCount >= event.getParticipantLimit()) {
            throwParticipantLimitException(confirmedRequestsCount, event.getParticipantLimit());
        }

        int limit = event.getParticipantLimit() - confirmedRequestsCount;

        List<RequestDto> confirmedRequests = new ArrayList<>();
        List<RequestDto> rejectedRequests = new ArrayList<>();
        for (Request request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                String message = String.format("Status is not in the right status. Current: %s", request.getStatus());
                log.error(message);
                throw new ForbiddenException(message);
            }

            if (limit > 0 && newStatus.equals(EventRequestStatus.CONFIRMED)) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(mapper.toDto(request));
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(mapper.toDto(request));
            }
            limit--;
        }
        requestRepository.saveAll(requests);
        return new EventRequestStatusUpdateResult(
                confirmedRequests,
                rejectedRequests
        );
    }

    private void throwParticipantLimitException(Integer confirmedRequests, Integer getParticipantLimit) {
        String message = String.format("The participant limit has been reached. confirmed: %d, limit %d", confirmedRequests, getParticipantLimit);
        log.error(message);
        throw new ForbiddenException(message);
    }


    private Event getEventByInitiatorAndEventId(Long userId, Long eventId) {
        User initiator = getUserById(userId);
        Event event = getEventById(eventId);
        if (!event.getInitiator().getId().equals(initiator.getId())) {
            String errorMessage = String.format("User isn't initiator, userId = %d, eventId = %d", userId, eventId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return event;
    }

    private Request getRequestById(Long requestId, User requester) {
        return requestRepository.findByIdAndRequester(requestId, requester)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Request not found, id = %d, requesterId = %d", requestId, requester.getId());
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
    }

    private User getUserById(Long userId) {
        return userService.getUserById(userId);
    }

    private Event getEventById(Long eventId) {
        return eventService.getEventById(eventId);
    }
}
