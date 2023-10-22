package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    List<User> getAll();

    User getById(Long id);

    User create(User user);

    User update(User user, Long id);

    User deleteById(Long id);

    void deleteAll();

    void checkForPresenceById(Long id);
}