package ru.practicum.shareit.user;

import java.util.List;

public interface UserRepository {
    List<User> getAll();

    User getById(Long id);

    User create(User user);

    User update(User user, Long id);

    User deleteById(Long id);

    void deleteAll();
}