package ru.practicum.shareit.user;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class UserMapper {
    public UserDto toUserDto(User user) {
        UserDto result = new UserDto(
                user.getName(),
                user.getEmail()
        );
        result.setId(user.getId());
        return result;
    }

    public List<UserDto> toUserDto(List<User> users) {
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    public User toUser(UserDto userDto) {
        User result = new User(
                userDto.getName(),
                userDto.getEmail()
        );
        result.setId(userDto.getId());
        return result;
    }
}