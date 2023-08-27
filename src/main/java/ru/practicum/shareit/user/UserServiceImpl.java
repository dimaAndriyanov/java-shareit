package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static ru.practicum.shareit.user.UserMapper.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Qualifier("userRepositoryDbImpl")
    private final UserRepository userRepository;
    @Qualifier("itemRepositoryDbImpl")
    private final ItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return toUserDto(userRepository.getAll());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        userRepository.checkForPresenceById(id);
        return toUserDto(userRepository.getById(id));
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        return toUserDto(userRepository.create(toUser(userDto)));
    }

    @Override
    @Transactional
    public UserDto updateUser(UserDto userDto, Long id) {
        userRepository.checkForPresenceById(id);
        return toUserDto(userRepository.update(toUser(userDto), id));
    }

    @Override
    @Transactional
    public UserDto deleteUserById(Long id) {
        userRepository.checkForPresenceById(id);
        itemRepository.deleteAllByOwnerId(id);
        return toUserDto(userRepository.deleteById(id));
    }

    @Override
    @Transactional
    public void deleteAllUsers() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}