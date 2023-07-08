package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EmailIsAlreadyInUseException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class UserRepositoryInMemoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        return users.get(id);
    }

    @Override
    public User create(User user) {
        if (user == null) {
            throw new NullPointerException("Can not create null user");
        }
        if (users.values().stream().map(User::getEmail).anyMatch(email -> email.equals(user.getEmail()))) {
            throw new EmailIsAlreadyInUseException(String.format("Email %s is already in use", user.getEmail()));
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("New user with id {} has been created", user.getId());
        return users.get(user.getId());
    }

    @Override
    public User update(User user, Long id) {
        if (user == null) {
            throw new NullPointerException("Can not update null user");
        }
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        if (user.getEmail() != null &&
                users.values().stream()
                .filter(savedUser -> !savedUser.getId().equals(id))
                .anyMatch(savedUser -> savedUser.getEmail().equals(user.getEmail()))) {
            throw new EmailIsAlreadyInUseException(String.format("Email %s is already in use", user.getEmail()));
        }
        User updatedUser = new User(
                user.getName() != null ? user.getName() : users.get(id).getName(),
                user.getEmail() != null ? user.getEmail() : users.get(id).getEmail()
        );
        updatedUser.setId(id);
        users.put(updatedUser.getId(), updatedUser);
        log.info("User with id {} has been updated", updatedUser.getId());
        return users.get(updatedUser.getId());
    }

    @Override
    public User deleteById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        User deletedUser = users.remove(id);
        log.info("User with id {} has been deleted", id);
        return deletedUser;
    }

    @Override
    public void deleteAll() {
        users.clear();
        log.info("All users has been deleted");
    }

    @Override
    public void checkForPresenceById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        if (!users.containsKey(id)) {
            throw new ObjectNotFoundException(String.format("User with id = %s not found", id));
        }
    }

    private Long getNextId() {
        return nextId++;
    }
}