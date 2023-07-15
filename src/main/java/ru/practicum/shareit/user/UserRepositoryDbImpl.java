package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EmailIsAlreadyInUseException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserRepositoryDbImpl implements UserRepository {
    private final UserDbInterface userDbInterface;

    @Override
    public List<User> getAll() {
        return userDbInterface.findAll();
    }

    @Override
    public User getById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        return userDbInterface.findById(id).get();
    }

    @Override
    public User create(User user) {
        if (user == null) {
            throw new NullPointerException("Can not create null user");
        }
        try {
            User result = userDbInterface.save(user);
            log.info("New user with id {} has been created", user.getId());
            return result;
        } catch (DataIntegrityViolationException exception) {
            throw new EmailIsAlreadyInUseException(String.format("Email %s is already in use", user.getEmail()));
        }
    }

    @Override
    public User update(User user, Long id) {
        if (user == null) {
            throw new NullPointerException("Can not update null user");
        }
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        User oldUser = userDbInterface.findById(id).get();
        User updatedUser = new User(
                user.getName() != null ? user.getName() : oldUser.getName(),
                user.getEmail() != null ? user.getEmail() : oldUser.getEmail()
        );
        updatedUser.setId(id);
        try {
            User result = userDbInterface.saveAndFlush(updatedUser);
            log.info("User with id {} has been updated", result.getId());
            return result;
        }  catch (DataIntegrityViolationException exception) {
            throw new EmailIsAlreadyInUseException(String.format("Email %s is already in use", user.getEmail()));
        }
    }

    @Override
    public User deleteById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        User deletedUser = userDbInterface.findById(id).get();
        userDbInterface.deleteById(id);
        log.info("User with id {} has been deleted", deletedUser.getId());
        return deletedUser;
    }

    @Override
    public void deleteAll() {
        userDbInterface.deleteAll();
        log.info("All users has been deleted");
    }

    @Override
    public void checkForPresenceById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        if (userDbInterface.findById(id).isEmpty()) {
            throw new ObjectNotFoundException(String.format("User with id = %s not found", id));
        }
    }
}