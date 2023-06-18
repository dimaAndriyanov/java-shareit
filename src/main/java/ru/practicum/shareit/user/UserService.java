package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User getUserById(Long id);

    User createUser(User user);

    User updateUser(User user, Long id);

    User deleteUserById(Long id);

    void deleteAllUsers();
}