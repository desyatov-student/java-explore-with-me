package ru.practicum.ewm.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toUser(NewUserRequest request);
}
