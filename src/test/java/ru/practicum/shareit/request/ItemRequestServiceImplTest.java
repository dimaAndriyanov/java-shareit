package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplTest {
    private final ItemRequestService itemRequestService;
    private final ItemService itemService;
    private final UserService userService;
    private final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    UserDto createUser(String name, String email) {
        return userService.createUser(new UserDto(name, email));
    }

    ItemDto createItem(Long requestId, Long ownerId) {
        return itemService.createItem(new ItemDto("itemName", "itemDescription", true, null, null, requestId), ownerId);
    }

    ItemRequestDto createItemRequest(String description, Long creatorId, LocalDateTime created) {
        return itemRequestService.createItemRequest(new ItemRequestDto(description, null), creatorId, created);
    }

    @Test
    void getAllItemRequestsByCreatorId() {
        UserDto creator = createUser("creatorName", "creator@mail.com");
        ItemRequestDto itemRequestWithItem = createItemRequest("firstDescription", creator.getId(), now.minusHours(1));
        UserDto owner = createUser("ownerName", "owner@mail.com");
        ItemDto item = createItem(itemRequestWithItem.getId(), owner.getId());
        ItemRequestDto itemRequestWithoutItems = createItemRequest("secondDescription", creator.getId(), now);

        List<ItemRequestDto> foundItemRequests = itemRequestService.getAllItemRequestsByCreatorId(creator.getId());
        assertThat(foundItemRequests, hasSize(2));
        assertThat(foundItemRequests.get(0).getId(), is(itemRequestWithoutItems.getId()));
        assertThat(foundItemRequests.get(0).getItems(), is(empty()));
        assertThat(foundItemRequests.get(1).getId(), is(itemRequestWithItem.getId()));
        assertThat(foundItemRequests.get(1).getItems(), hasSize(1));
        assertThat(foundItemRequests.get(1).getItems(), hasItem(item));

        foundItemRequests = itemRequestService.getAllItemRequestsByCreatorId(owner.getId());
        assertThat(foundItemRequests, is(empty()));
    }

    @Test
    void getAllItemRequestsByUserId() {
        UserDto creator = createUser("creatorName", "creator@mail.com");
        ItemRequestDto itemRequestWithItem = createItemRequest("firstDescription", creator.getId(), now.plusHours(1));
        UserDto owner = createUser("ownerName", "owner@mail.com");
        ItemDto item = createItem(itemRequestWithItem.getId(), owner.getId());
        ItemRequestDto itemRequestWithoutItems = createItemRequest("secondDescription", creator.getId(), now);

        List<ItemRequestDto> foundItemRequests = itemRequestService.getAllItemRequestsByUserId(owner.getId(), 0, 10);
        assertThat(foundItemRequests, hasSize(2));
        assertThat(foundItemRequests.get(0).getId(), is(itemRequestWithItem.getId()));
        assertThat(foundItemRequests.get(0).getItems(), hasSize(1));
        assertThat(foundItemRequests.get(0).getItems(), hasItem(item));
        assertThat(foundItemRequests.get(1).getId(), is(itemRequestWithoutItems.getId()));
        assertThat(foundItemRequests.get(1).getItems(), is(empty()));

        foundItemRequests = itemRequestService.getAllItemRequestsByUserId(creator.getId(), 0, 10);
        assertThat(foundItemRequests, is(empty()));
    }

    @Test
    void GetAllItemRequestsByUserIdPageable() {
        UserDto creator = createUser("creatorName", "creator@mail.com");
        ItemRequestDto firstItemRequest = createItemRequest("firstDescription", creator.getId(), now.plusHours(1));
        ItemRequestDto secondItemRequest = createItemRequest("secondDescription", creator.getId(), now.plusHours(3));
        ItemRequestDto thirdItemRequest = createItemRequest("thirdDescription", creator.getId(), now);
        UserDto user = createUser("userName", "user@mail.com");

        List<ItemRequestDto> foundItemRequests = itemRequestService.getAllItemRequestsByUserId(user.getId(), 0, 1);
        assertThat(foundItemRequests, hasSize(1));
        assertThat(foundItemRequests.get(0), is(secondItemRequest));

        foundItemRequests = itemRequestService.getAllItemRequestsByUserId(user.getId(), 1, 2);
        assertThat(foundItemRequests, hasSize(2));
        assertThat(foundItemRequests.get(0), is(secondItemRequest));
        assertThat(foundItemRequests.get(1), is(firstItemRequest));

        foundItemRequests = itemRequestService.getAllItemRequestsByUserId(user.getId(), 2, 3);
        assertThat(foundItemRequests, hasSize(3));
        assertThat(foundItemRequests.get(0), is(secondItemRequest));
        assertThat(foundItemRequests.get(1), is(firstItemRequest));
        assertThat(foundItemRequests.get(2), is(thirdItemRequest));

        foundItemRequests = itemRequestService.getAllItemRequestsByUserId(user.getId(), 3, 3);
        assertThat(foundItemRequests, is(empty()));
    }

    @Test
    void getItemRequestById() {
        UserDto creator = createUser("creatorName", "creator@mail.com");
        ItemRequestDto itemRequestWithItem = createItemRequest("firstDescription", creator.getId(), now.plusHours(1));
        UserDto owner = createUser("ownerName", "owner@mail.com");
        ItemDto item = createItem(itemRequestWithItem.getId(), owner.getId());
        ItemRequestDto itemRequestWithoutItems = createItemRequest("secondDescription", creator.getId(), now);
        UserDto user = createUser("userName", "user@mail.com");

        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getItemRequestById(9999L, user.getId()));
        assertThat(objectNotFoundException.getMessage(), is("Item request with id = 9999 not found"));

        ItemRequestDto foundItemRequest =
                itemRequestService.getItemRequestById(itemRequestWithItem.getId(), user.getId());
        assertThat(foundItemRequest.getId(), is(itemRequestWithItem.getId()));
        assertThat(foundItemRequest.getDescription(), is(itemRequestWithItem.getDescription()));
        assertThat(foundItemRequest.getCreated(), is(itemRequestWithItem.getCreated()));
        assertThat(foundItemRequest.getItems(), hasSize(1));
        assertThat(foundItemRequest.getItems(), hasItem(item));

        foundItemRequest = itemRequestService.getItemRequestById(itemRequestWithoutItems.getId(), user.getId());
        assertThat(foundItemRequest.getId(), is(itemRequestWithoutItems.getId()));
        assertThat(foundItemRequest.getDescription(), is(itemRequestWithoutItems.getDescription()));
        assertThat(foundItemRequest.getCreated(), is(itemRequestWithoutItems.getCreated()));
        assertThat(foundItemRequest.getItems(), hasSize(0));
    }

    @Test
    void createItemRequest() {
        UserDto creator = createUser("creatorName", "creator@mail.com");
        ItemRequestDto createdItemRequest = createItemRequest("description", creator.getId(), now);
        assertThat(createdItemRequest.getId(), not(nullValue()));
        assertThat(createdItemRequest.getDescription(), not(nullValue()));
        assertThat(createdItemRequest.getDescription(), is("description"));
        assertThat(createdItemRequest.getCreated(), not(nullValue()));
        assertThat(createdItemRequest.getCreated(), is(now));
        assertThat(createdItemRequest.getItems(), not(nullValue()));
        assertThat(createdItemRequest.getItems(), is(empty()));
    }
}