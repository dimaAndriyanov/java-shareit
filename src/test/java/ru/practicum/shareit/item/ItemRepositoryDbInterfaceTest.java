package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepositoryDbInterface;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRepositoryDbInterfaceTest {
    private final ItemRepositoryDbInterface itemRepository;

    private final UserRepositoryDbInterface userRepository;

    private final ItemRequestRepository itemRequestRepository;

    private final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    User saveUser(String name, String email) {
        return userRepository.save(new User(name, email));
    }

    ItemRequest saveItemRequest(User creator) {
        return itemRequestRepository.save(new ItemRequest("itemRequestDescription", now, creator));
    }

    Item saveItem(User owner) {
        return itemRepository.save(new Item("itemName", "itemDescription", true, owner, null));
    }

    @Test
    void shouldBeManagedByEntityManagerWhenSave() {
        User owner = saveUser("ownerName", "ownerEmail");
        User requestCreator = saveUser("requestCreatorName", "requestCreatorName");
        ItemRequest itemRequest = saveItemRequest(requestCreator);

        Item itemWithRequest = new Item("firstItemName", "firstItemDescription", true, owner, itemRequest);
        Item itemWithoutRequests = new Item("secondItemName", "secondItemDescription", false, owner, null);

        assertThat(itemWithRequest.getId(), nullValue());
        assertThat(itemWithoutRequests.getId(), nullValue());

        Item savedItemWithRequest = itemRepository.save(itemWithRequest);
        Item savedItemWithoutRequests = itemRepository.save(itemWithoutRequests);

        assertThat(itemWithRequest.getId(), not(nullValue()));
        assertThat(itemWithoutRequests.getId(), not(nullValue()));

        assertThat(savedItemWithRequest, is(itemWithRequest));
        assertThat(savedItemWithoutRequests, is(itemWithoutRequests));
    }

    @Test
    void shouldReturnItemWithOwnerWhenFindByIdWithOwner() {
        User owner = saveUser("ownerName", "ownerEmail");
        Item item = saveItem(owner);

        Optional<Item> foundItem = itemRepository.findByIdWithOwner(item.getId());
        assertTrue(foundItem.isPresent());
        assertThat(foundItem.get(), is(item));
        assertThat(foundItem.get().getOwner(), is(owner));

        foundItem = itemRepository.findByIdWithOwner(item.getId() + 9999L);
        assertTrue(foundItem.isEmpty());
    }
}