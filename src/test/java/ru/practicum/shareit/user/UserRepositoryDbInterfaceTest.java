package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserRepositoryDbInterfaceTest {
    private final UserRepositoryDbInterface userRepository;

    @Test
    void save() {
        User user = new User("userName", "userEmail");

        assertThat(user.getId(), nullValue());

        User savedUser = userRepository.save(user);

        assertThat(user.getId(), not(nullValue()));
        assertThat(savedUser, is(user));
    }
}