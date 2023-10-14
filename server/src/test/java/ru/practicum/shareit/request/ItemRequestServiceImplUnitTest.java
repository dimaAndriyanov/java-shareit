package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.item.ItemMapper.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplUnitTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    ItemRequestService getItemRequestService() {
        return new ItemRequestServiceImpl(
                itemRequestRepository, userRepository, itemRepository
        );
    }

    User createUser(String name, String email, Long id) {
        User result = new User(name, email);
        result.setId(id);
        return result;
    }

    ItemRequest createItemRequest(String description, LocalDateTime created, User requestCreator, Long id) {
        ItemRequest result = new ItemRequest(description, created, requestCreator);
        result.setId(id);
        return result;
    }

    Item createItem(String name, String description, User owner, ItemRequest itemRequest, Long id) {
        Item result = new Item(name, description, true, owner, itemRequest);
        result.setId(id);
        return result;
    }

    @Test
    void shouldThrowObjectNotFoundExceptionWhenGetAllItemRequestsByCreatorIdWhenCreatorNotFound() {
        ItemRequestService itemRequestService = getItemRequestService();

        doThrow(new ObjectNotFoundException("objectNotFoundException"))
                .when(userRepository).checkForPresenceById(9999L);
        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getAllItemRequestsByCreatorId(9999L));
        assertThat(objectNotFoundException.getMessage(), is("objectNotFoundException"));
    }

    @Test
    void shouldReturnListOfItemRequestsSortedByCreatedDescendingWhenGetAllItemRequestsByCreatorId() {
        ItemRequestService itemRequestService = getItemRequestService();

        when(itemRequestRepository.findAllByCreatorIdOrderByCreatedDesc(17L))
                .thenReturn(List.of());
        when(itemRepository.getAllItemsByRequestIds(List.of()))
                .thenReturn(List.of());
        List<ItemRequestDto> foundItemRequests = itemRequestService.getAllItemRequestsByCreatorId(17L);
        assertThat(foundItemRequests, is(empty()));

        User requestCreator = createUser("creatorName", "creatorEmail", 25L);

        ItemRequest firstItemRequest = createItemRequest("firstDescription", now.minusDays(3), requestCreator, 19L);
        ItemRequest secondItemRequest = createItemRequest("secondDescription", now.minusDays(2), requestCreator, 45L);
        ItemRequest thirdItemRequest = createItemRequest("thirdDescription", now.minusDays(1), requestCreator, 82L);

        when(itemRequestRepository.findAllByCreatorIdOrderByCreatedDesc(requestCreator.getId()))
                .thenReturn(List.of(thirdItemRequest, secondItemRequest, firstItemRequest));

        User owner = createUser("ownerName", "ownerEmail", 42L);

        Item firstItem = createItem("firstItemName", "firstItemDescription", owner, firstItemRequest, 14L);
        Item secondItem = createItem("secondItemName", "secondItemDescription", owner, firstItemRequest, 19L);
        Item thirdItem = createItem("thirdItemName", "thirdItemDescription", owner, secondItemRequest, 26L);

        when(itemRepository.getAllItemsByRequestIds(List.of(thirdItemRequest.getId(), secondItemRequest.getId(),
                firstItemRequest.getId())))
                .thenReturn(List.of(firstItem, secondItem, thirdItem));

        foundItemRequests = itemRequestService.getAllItemRequestsByCreatorId(requestCreator.getId());
        assertThat(foundItemRequests, hasSize(3));
        assertThat(foundItemRequests.get(0).getId(), is(thirdItemRequest.getId()));
        assertThat(foundItemRequests.get(0).getItems(), is(empty()));
        assertThat(foundItemRequests.get(1).getId(), is(secondItemRequest.getId()));
        assertThat(foundItemRequests.get(1).getItems(), hasSize(1));
        assertThat(foundItemRequests.get(1).getItems(), hasItem(toItemDto(thirdItem, null, null)));
        assertThat(foundItemRequests.get(2).getId(), is(firstItemRequest.getId()));
        assertThat(foundItemRequests.get(2).getItems(), hasSize(2));
        assertThat(foundItemRequests.get(2).getItems(), hasItems(
                toItemDto(firstItem, null, null), toItemDto(secondItem, null, null)
        ));
    }

    @Test
    void shouldThrowObjectNotFoundExceptionWhenGetAllItemRequestsByUserIdWhenUserNotFound() {
        ItemRequestService itemRequestService = getItemRequestService();

        doThrow(new ObjectNotFoundException("objectNotFoundException"))
                .when(userRepository).checkForPresenceById(9999L);
        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getAllItemRequestsByUserId(9999L, 0, 10));
        assertThat(objectNotFoundException.getMessage(), is("objectNotFoundException"));
    }

    @Test
    void shouldReturnListOfItemRequestsSortedByCreatedDescendingWhenGetAllItemRequestsByUserId() {
        ItemRequestService itemRequestService = getItemRequestService();

        when(itemRequestRepository.findAllByCreatorIdNotOrderByCreatedDesc(17L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of()));
        when(itemRepository.getAllItemsByRequestIds(List.of()))
                .thenReturn(List.of());
        List<ItemRequestDto> foundItemRequests = itemRequestService.getAllItemRequestsByUserId(17L, 0, 10);
        assertThat(foundItemRequests, is(empty()));

        User requestCreator = createUser("creatorName", "creatorEmail", 25L);

        ItemRequest firstItemRequest = createItemRequest("firstDescription", now.minusDays(3), requestCreator, 19L);
        ItemRequest secondItemRequest = createItemRequest("secondDescription", now.minusDays(2), requestCreator, 45L);
        ItemRequest thirdItemRequest = createItemRequest("thirdDescription", now.minusDays(1), requestCreator, 82L);

        User user = createUser("userName", "userEmail", 32L);

        when(itemRequestRepository.findAllByCreatorIdNotOrderByCreatedDesc(user.getId(), PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(thirdItemRequest, secondItemRequest, firstItemRequest)));

        User owner = createUser("ownerName", "ownerEmail", 42L);

        Item firstItem = createItem("firstItemName", "firstItemDescription", owner, firstItemRequest, 14L);
        Item secondItem = createItem("secondItemName", "secondItemDescription", owner, firstItemRequest, 19L);
        Item thirdItem = createItem("thirdItemName", "thirdItemDescription", owner, secondItemRequest, 26L);

        when(itemRepository.getAllItemsByRequestIds(List.of(thirdItemRequest.getId(), secondItemRequest.getId(),
                firstItemRequest.getId())))
                .thenReturn(List.of(firstItem, secondItem, thirdItem));

        foundItemRequests = itemRequestService.getAllItemRequestsByUserId(user.getId(), 0, 10);
        assertThat(foundItemRequests, hasSize(3));
        assertThat(foundItemRequests.get(0).getId(), is(thirdItemRequest.getId()));
        assertThat(foundItemRequests.get(0).getItems(), is(empty()));
        assertThat(foundItemRequests.get(1).getId(), is(secondItemRequest.getId()));
        assertThat(foundItemRequests.get(1).getItems(), hasSize(1));
        assertThat(foundItemRequests.get(1).getItems(), hasItem(toItemDto(thirdItem, null, null)));
        assertThat(foundItemRequests.get(2).getId(), is(firstItemRequest.getId()));
        assertThat(foundItemRequests.get(2).getItems(), hasSize(2));
        assertThat(foundItemRequests.get(2).getItems(), hasItems(
                toItemDto(firstItem, null, null), toItemDto(secondItem, null, null)
        ));
    }

    @Test
    void shouldThrowObjectNotFoundExceptionWhenGetItemRequestByIdWithUserNotFound() {
        ItemRequestService itemRequestService = getItemRequestService();

        doThrow(new ObjectNotFoundException("objectNotFoundException"))
                .when(userRepository).checkForPresenceById(9999L);
        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getItemRequestById(1L, 9999L));
        assertThat(objectNotFoundException.getMessage(), is("objectNotFoundException"));
    }

    @Test
    void shouldThrowObjectNotFoundExceptionWhenGetItemRequestByIdWithItemRequestNotFound() {
        ItemRequestService itemRequestService = getItemRequestService();

        when(itemRequestRepository.findById(17L))
                .thenReturn(Optional.empty());
        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getItemRequestById(17L, 19L));
        assertThat(objectNotFoundException.getMessage(), is("Item request with id = 17 not found"));
    }

    @Test
    void shouldReturnListOfItemRequestsWhenGetItemRequestById() {
        ItemRequestService itemRequestService = getItemRequestService();

        User requestCreator = createUser("creatorName", "creatorEmail", 25L);
        ItemRequest itemRequestWithItem = createItemRequest("firstDescription", now.minusDays(3), requestCreator, 19L);
        ItemRequest itemRequestWithoutItems =
                createItemRequest("secondDescription", now.minusDays(3), requestCreator, 23L);
        User owner = createUser("ownerName", "ownerEmail", 42L);
        Item item = createItem("itemName", "itemDescription", owner, itemRequestWithItem, 14L);
        User user = createUser("userName", "userEmail", 54L);

        when(itemRequestRepository.findById(itemRequestWithItem.getId()))
                .thenReturn(Optional.of(itemRequestWithItem));
        when(itemRepository.getAllItemsByRequestId(itemRequestWithItem.getId()))
                .thenReturn(List.of(item));

        ItemRequestDto foundItemRequest =
                itemRequestService.getItemRequestById(itemRequestWithItem.getId(), user.getId());
        assertThat(foundItemRequest.getId(), is(itemRequestWithItem.getId()));
        assertThat(foundItemRequest.getItems(), hasSize(1));
        assertThat(foundItemRequest.getItems(), hasItem(toItemDto(item, null, null)));

        when(itemRequestRepository.findById(itemRequestWithoutItems.getId()))
                .thenReturn(Optional.of(itemRequestWithoutItems));
        when(itemRepository.getAllItemsByRequestId(itemRequestWithoutItems.getId()))
                .thenReturn(List.of());

        foundItemRequest = itemRequestService.getItemRequestById(itemRequestWithoutItems.getId(), user.getId());
        assertThat(foundItemRequest.getId(), is(itemRequestWithoutItems.getId()));
        assertThat(foundItemRequest.getItems(), is(empty()));
    }

    @Test
    void shouldThrowObjectNotFoundExceptionWhenCreateItemRequestWithUserNotFound() {
        ItemRequestService itemRequestService = getItemRequestService();

        doThrow(new ObjectNotFoundException("objectNotFoundException"))
                .when(userRepository).checkForPresenceById(9999L);
        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.createItemRequest(new ItemRequestDto("description", null), 9999L, now));
        assertThat(objectNotFoundException.getMessage(), is("objectNotFoundException"));
    }

    @Test
    void shouldReturnCreatedItemRequestWhenCreateItemRequest() {
        ItemRequestService itemRequestService = getItemRequestService();

        User requestCreator = createUser("creatorName", "creatorEmail", 25L);
        when(userRepository.getById(requestCreator.getId()))
                .thenReturn(requestCreator);
        ItemRequest itemRequest = new ItemRequest("description", now, requestCreator);
        ItemRequest savedItemRequest = createItemRequest(itemRequest.getDescription(),
                itemRequest.getCreated(), itemRequest.getCreator(), 19L);
        when(itemRequestRepository.save(itemRequest))
                .thenReturn(savedItemRequest);

        ItemRequestDto createdItemRequest = itemRequestService.createItemRequest(
                new ItemRequestDto("description", null), requestCreator.getId(), now);
        assertThat(createdItemRequest.getId(), is(savedItemRequest.getId()));
        assertThat(createdItemRequest.getDescription(), is(savedItemRequest.getDescription()));
        assertThat(createdItemRequest.getCreated(), is(savedItemRequest.getCreated().format(formatter)));
        assertThat(createdItemRequest.getItems(), is(empty()));
    }
}