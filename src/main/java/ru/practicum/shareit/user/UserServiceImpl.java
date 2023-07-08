package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static ru.practicum.shareit.user.UserMapper.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Qualifier("userRepositoryInMemoryImpl")
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return toUserDto(userRepository.getAll());
    }

    @Override
    public UserDto getUserById(Long id) {
        userRepository.checkForPresenceById(id);
        return toUserDto(userRepository.getById(id));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        return toUserDto(userRepository.create(toUser(userDto)));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long id) {
        userRepository.checkForPresenceById(id);
        return toUserDto(userRepository.update(toUser(userDto), id));
    }

    @Override
    public UserDto deleteUserById(Long id) {
        userRepository.checkForPresenceById(id);
        itemRepository.deleteAllByOwnerId(id);
        return toUserDto(userRepository.deleteById(id));
    }

    @Override
    public void deleteAllUsers() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}