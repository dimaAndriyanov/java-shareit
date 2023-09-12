package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepositoryDbInterface;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestRepositoryTest {
    private final ItemRequestRepository itemRequestRepository;

    private final UserRepositoryDbInterface userRepository;

    private final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    User saveRequestCreator() {
        return userRepository.save(new User("requestCreatorName", "requestCreatorEmail"));
    }

    @Test
    void shouldBeManagedByEntityManagerWhenSave() {
        User requestCreator = saveRequestCreator();

        ItemRequest itemRequest = new ItemRequest("description", now, requestCreator);

        assertThat(itemRequest.getId(), nullValue());

        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);

        assertThat(itemRequest.getId(), not(nullValue()));
        assertThat(savedItemRequest, is(itemRequest));
    }
}