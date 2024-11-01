package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.GetCommentsRequest;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.ewm.utils.DateMapper;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final UserService userService;
    private final EventService eventService;
    private final CommentRepository commentRepository;
    private final CommentMapper mapper;
    private final DateMapper dateMapper;

    public List<CommentDto> getAuthorComments(Long authorId, GetCommentsRequest request) {
        User author = getUserById(authorId);
        Integer from = request.getFrom();
        Integer size = request.getSize();
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(DESC, "created"));
        return commentRepository.findByAuthor(author, page).stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<CommentDto> getEventComments(Long eventId, GetCommentsRequest request) {
        Event event = getEventById(eventId);
        Integer from = request.getFrom();
        Integer size = request.getSize();
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(DESC, "created"));
        return commentRepository.findByEvent(event, page).stream()
                .map(mapper::toDto)
                .toList();
    }

    public CommentDto getById(Long commentId) {
        return mapper.toDto(getCommentById(commentId));
    }

    @Transactional
    public CommentDto create(Long authorId, Long eventId, NewCommentDto request) {
        Event event = getEventById(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            String errorMessage = "Cannot create comment because it's not in the right state: PUBLISHED, current: " + event.getState();
            log.error(errorMessage);
            throw new ForbiddenException(errorMessage);
        }
        User author = getUserById(authorId);
        Comment comment = mapper.toModel(request, author, event, dateMapper.now());
        comment = commentRepository.save(comment);
        log.info("Creating comment is successful: {}", comment);
        return mapper.toDto(comment);
    }

    @Transactional
    public void remove(Long commentId) {
        Comment comment = getCommentById(commentId);
        commentRepository.delete(comment);
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Comment not found, id = %d", commentId);
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
