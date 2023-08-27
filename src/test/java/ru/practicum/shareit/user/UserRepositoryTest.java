package ru.practicum.shareit.user;

import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import ru.practicum.shareit.exception.EmailIsAlreadyInUseException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Setter
abstract class UserRepositoryTest {
    private UserRepository userRepository;

    @BeforeEach
    void clearUserRepository() {
        userRepository.deleteAll();
    }

    List<User> createThreeUsers() {
        return List.of(
            userRepository.create(new User("Dmitriy", "my.email@mail.com")),
            userRepository.create(new User("Ekaterina", "ekaterine@mail.org")),
            userRepository.create(new User("Pashka", "big_boss@mail.ru"))
        );
    }

    void getAll() {
        List<User> savedUsers = userRepository.getAll();

        assertNotNull(savedUsers);
        assertTrue(savedUsers.isEmpty());

        List<User> addedUsers = createThreeUsers();
        savedUsers = userRepository.getAll();

        assertNotNull(savedUsers);
        assertEquals(3, savedUsers.size());
        assertEquals(new HashSet<>(addedUsers), new HashSet<>(savedUsers));
    }

    void getById() {
        List<User> addedUsers = createThreeUsers();

        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> userRepository.getById(null));
        assertEquals("Id must not be null", nullPointerException.getMessage());

        assertEquals(addedUsers.get(1), userRepository.getById(addedUsers.get(1).getId()));
    }

    void create() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> userRepository.create(null));
        assertEquals("Can not create null user", nullPointerException.getMessage());

        createThreeUsers();

        User userWithEmailAlreadyInUse = new User("Maria", "big_boss@mail.ru");
        EmailIsAlreadyInUseException emailIsAlreadyInUseException = assertThrows(EmailIsAlreadyInUseException.class,
                () -> userRepository.create(userWithEmailAlreadyInUse));
        assertEquals("Email big_boss@mail.ru is already in use", emailIsAlreadyInUseException.getMessage());

        User newUser = userRepository.create(new User("Maria", "big.boss@mail.ru"));
        assertNotNull(userRepository.getById(newUser.getId()));
        assertEquals(newUser, userRepository.getById(newUser.getId()));
    }

    void update() {
        List<User> addedUsers = createThreeUsers();

        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> userRepository.update(null, 1L));
        assertEquals("Can not update null user", nullPointerException.getMessage());

        nullPointerException = assertThrows(NullPointerException.class,
                () -> userRepository.update(new User("updatedName", "updatedEmail"), null));
        assertEquals("Id must not be null", nullPointerException.getMessage());

        User userWithEmailAlreadyInUse = new User("Katya", "my.email@mail.com");
        EmailIsAlreadyInUseException emailIsAlreadyInUseException = assertThrows(EmailIsAlreadyInUseException.class,
                () -> userRepository.update(userWithEmailAlreadyInUse, addedUsers.get(1).getId()));
        assertEquals("Email my.email@mail.com is already in use", emailIsAlreadyInUseException.getMessage());

        User dmitriyWithUpdatedEmail =
                userRepository.update(new User(null, "my.new.email@mail.com"), addedUsers.get(0).getId());
        assertEquals(dmitriyWithUpdatedEmail, userRepository.getById(addedUsers.get(0).getId()));
        assertEquals(dmitriyWithUpdatedEmail.getName(), addedUsers.get(0).getName());
        assertEquals(dmitriyWithUpdatedEmail.getEmail(), "my.new.email@mail.com");

        assertDoesNotThrow(
                () -> userRepository.update(new User(null, "my.new.email@mail.com"), addedUsers.get(0).getId()));

        User ekaterinaWithUpdatedName = userRepository.update(new User("Katya", null), addedUsers.get(1).getId());
        assertEquals(ekaterinaWithUpdatedName.getName(), "Katya");
        assertEquals(ekaterinaWithUpdatedName.getEmail(), addedUsers.get(1).getEmail());

        User pavelWithUpdatedNameAndEmail =
                userRepository.update(new User("Pavel", "Pavlu@mail.ru"), addedUsers.get(2).getId());
        assertEquals(pavelWithUpdatedNameAndEmail.getName(), "Pavel");
        assertEquals(pavelWithUpdatedNameAndEmail.getEmail(), "Pavlu@mail.ru");
    }

    void deleteById() {
        List<User> addedUsers = createThreeUsers();

        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> userRepository.deleteById(null));
        assertEquals("Id must not be null", nullPointerException.getMessage());

        assertEquals(3, userRepository.getAll().size());
        User deletedUser = userRepository.deleteById(addedUsers.get(1).getId());
        assertEquals(addedUsers.get(1), deletedUser);
        assertEquals(2, userRepository.getAll().size());
        assertThrows(ObjectNotFoundException.class,
                () -> userRepository.checkForPresenceById(addedUsers.get(1).getId()));
    }

    void deleteAll() {
        List<User> addedUsers = createThreeUsers();

        assertEquals(3, userRepository.getAll().size());
        assertDoesNotThrow(() -> userRepository.checkForPresenceById(addedUsers.get(1).getId()));

        userRepository.deleteAll();

        assertTrue(userRepository.getAll().isEmpty());
        assertThrows(ObjectNotFoundException.class,
                () -> userRepository.checkForPresenceById(addedUsers.get(1).getId()));
    }

    void checkForPresence() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> userRepository.checkForPresenceById(null));
        assertEquals("Id must not be null", nullPointerException.getMessage());

        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> userRepository.checkForPresenceById(9999L));
        assertEquals("User with id = 9999 not found", objectNotFoundException.getMessage());

        List<User> addedUsers = createThreeUsers();
        addedUsers.forEach(user -> assertDoesNotThrow(() -> userRepository.checkForPresenceById(user.getId())));
    }
}