package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dto.GetUsersRequest;
import ru.practicum.ewm.user.dto.NewUserDto;
import ru.practicum.ewm.user.dto.UserFullDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserFullDto> getUsers(GetUsersRequest request) {
        List<Long> ids = request.getIds();
        Integer from = request.getFrom();
        Integer size = request.getSize();
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);

        Supplier<Page<User>> getUsers;
        if (request.getIds().isEmpty()) {
            getUsers = () -> userRepository.findAll(page);
        } else {
            getUsers = () -> userRepository.findByIdIn(ids, page);
        }
        return getUsers.get()
                .map(userMapper::toDto)
                .toList();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Пользователь с id = %d не найден", userId);
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
    }

    public UserFullDto create(NewUserDto request) {
        Optional<User> alreadyExistUser = userRepository.findByEmail(request.getEmail());
        if (alreadyExistUser.isPresent()) {
            log.error("Creating user is failed. email = {} exists", request.getEmail());
            throw new DuplicatedDataException("User with email = " + request.getEmail() + " exists");
        }

        User user = userMapper.toUser(request);
        user = userRepository.save(user);
        log.info("Creating user is successful: {}", user);
        return userMapper.toDto(user);
    }

    public void removeUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }
}
