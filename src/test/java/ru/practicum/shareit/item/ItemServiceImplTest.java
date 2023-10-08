package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.SentBookingDto;
import ru.practicum.shareit.exception.DataAccessException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.PostingCommentWithoutCompletedBookingException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemServiceImplTest {
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private final ItemRequestService itemRequestService;

    @Autowired
    ItemServiceImplTest(ItemService itemService, UserService userService,BookingService bookingService,
                        ItemRequestService itemRequestService) {
        this.itemService = itemService;
        this.userService = userService;
        this.bookingService = bookingService;
        this.itemRequestService = itemRequestService;
    }

    List<UserDto> setUsers() {
        return List.of(
                userService.createUser(new UserDto("user1", "email1@mail.com")),
                userService.createUser(new UserDto("user2", "email2@mail.com")),
                userService.createUser(new UserDto("user3", "email3@mail.com"))
        );
    }

    List<ItemDto> setItems(List<UserDto> users) {
        return List.of(
                itemService.createItem(new ItemDto("name1", "desc1", true, null, null, null), users.get(0).getId()),
                itemService.createItem(new ItemDto("name2", "desc2", true, null, null, null), users.get(0).getId()),
                itemService.createItem(new ItemDto("name3", "desc3", true, null, null, null), users.get(0).getId()),
                itemService.createItem(new ItemDto("name4", "desc3", true, null, null, null), users.get(0).getId())
        );
    }

    List<SentBookingDto> setBookings(List<UserDto> users, List<ItemDto> items) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<SentBookingDto> result = List.of(
                bookingService.createBooking(items.get(0).getId(), users.get(1).getId(),
                        List.of(now.minusDays(2), now.minusDays(1))),
                bookingService.createBooking(items.get(0).getId(), users.get(1).getId(),
                        List.of(now.minusHours(1), now.minusMinutes(30))),
                bookingService.createBooking(items.get(0).getId(), users.get(1).getId(),
                        List.of(now.minusMinutes(1), now.plusMinutes(1))),
                bookingService.createBooking(items.get(0).getId(), users.get(1).getId(),
                        List.of(now.plusMinutes(30), now.plusHours(1))),
                bookingService.createBooking(items.get(0).getId(), users.get(1).getId(),
                        List.of(now.plusDays(1), now.plusDays(2))),

                bookingService.createBooking(items.get(1).getId(), users.get(1).getId(),
                        List.of(now.minusDays(2), now.minusDays(1))),

                bookingService.createBooking(items.get(2).getId(), users.get(1).getId(),
                        List.of(now.plusDays(1), now.plusDays(2))),

                bookingService.createBooking(items.get(0).getId(), users.get(2).getId(),
                        List.of(now.minusDays(2), now.minusDays(1)))
        );
        result.forEach(booking -> bookingService.updateBookingStatus(booking.getId(), users.get(0).getId(), true));
        return result;
    }

    List<CommentDto> setComments(List<UserDto> users, List<ItemDto> items) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        return List.of(
                itemService.createComment(new CommentDto("comment1", null, null),
                        items.get(0).getId(), users.get(1).getId(), now),
                itemService.createComment(new CommentDto("comment2", null, null),
                        items.get(1).getId(), users.get(1).getId(), now),
                itemService.createComment(new CommentDto("comment1", null, null),
                        items.get(0).getId(), users.get(2).getId(), now)
        );
    }

    @Test
    void getItemById() {
        List<UserDto> users = setUsers();
        List<ItemDto> items = setItems(users);
        List<SentBookingDto> bookings = setBookings(users, items);
        List<CommentDto> comments = setComments(users, items);

        // test bookings in get items by owner
        ItemDto item1 = itemService.getItemById(items.get(0).getId(), users.get(0).getId());
        assertNotNull(item1.getLastBooking());
        assertEquals(bookings.get(2).getId(), item1.getLastBooking().getId());
        assertNotNull(item1.getNextBooking());
        assertEquals(bookings.get(3).getId(), item1.getNextBooking().getId());

        ItemDto item2 = itemService.getItemById(items.get(1).getId(), users.get(0).getId());
        assertNotNull(item2.getLastBooking());
        assertEquals(bookings.get(5).getId(), item2.getLastBooking().getId());
        assertNull(item2.getNextBooking());

        ItemDto item3 = itemService.getItemById(items.get(2).getId(), users.get(0).getId());
        assertNull(item3.getLastBooking());
        assertNotNull(item3.getNextBooking());
        assertEquals(bookings.get(6).getId(), item3.getNextBooking().getId());

        ItemDto item4 = itemService.getItemById(items.get(3).getId(), users.get(0).getId());
        assertNull(item4.getLastBooking());
        assertNull(item4.getNextBooking());

        // test bookings in get item by not owner
        item1 = itemService.getItemById(items.get(0).getId(), users.get(1).getId());
        assertNull(item1.getLastBooking());
        assertNull(item1.getNextBooking());

        // test comments in get item by owner
        item1 = itemService.getItemById(items.get(0).getId(), users.get(0).getId());
        assertEquals(2, item1.getComments().size());
        assertEquals(Set.of(comments.get(0).getId(), comments.get(2).getId()),
                item1.getComments().stream().map(CommentDto::getId).collect(Collectors.toSet()));

        item2 = itemService.getItemById(items.get(1).getId(), users.get(0).getId());
        assertEquals(1, item2.getComments().size());
        assertEquals(comments.get(1).getId(), item2.getComments().get(0).getId());

        item3 = itemService.getItemById(items.get(2).getId(), users.get(0).getId());
        assertTrue(item3.getComments().isEmpty());

        // test comments in get item by not owner
        item1 = itemService.getItemById(items.get(0).getId(), users.get(1).getId());
        assertEquals(2, item1.getComments().size());
        assertEquals(Set.of(comments.get(0).getId(), comments.get(2).getId()),
                item1.getComments().stream().map(CommentDto::getId).collect(Collectors.toSet()));

        item2 = itemService.getItemById(items.get(1).getId(), users.get(1).getId());
        assertEquals(1, item2.getComments().size());
        assertEquals(comments.get(1).getId(), item2.getComments().get(0).getId());

        item3 = itemService.getItemById(items.get(2).getId(), users.get(1).getId());
        assertTrue(item3.getComments().isEmpty());
    }

    @Test
    void getAllItemsByOwnerId() {
        List<UserDto> users = setUsers();
        List<ItemDto> items = setItems(users);
        List<SentBookingDto> bookings = setBookings(users, items);
        List<CommentDto> comments = setComments(users, items);

        List<ItemDto> foundItems = itemService.getAllItemsByOwnerId(users.get(0).getId(), 0, 10);
        ItemDto item1 = foundItems.stream().filter(item -> item.getId().equals(items.get(0).getId())).findAny().get();
        ItemDto item2 = foundItems.stream().filter(item -> item.getId().equals(items.get(1).getId())).findAny().get();
        ItemDto item3 = foundItems.stream().filter(item -> item.getId().equals(items.get(2).getId())).findAny().get();
        ItemDto item4 = foundItems.stream().filter(item -> item.getId().equals(items.get(3).getId())).findAny().get();

        assertEquals(bookings.get(2).getId(), item1.getLastBooking().getId());
        assertNotNull(item1.getNextBooking());
        assertEquals(bookings.get(3).getId(), item1.getNextBooking().getId());

        assertNotNull(item2.getLastBooking());
        assertEquals(bookings.get(5).getId(), item2.getLastBooking().getId());
        assertNull(item2.getNextBooking());

        assertNull(item3.getLastBooking());
        assertNotNull(item3.getNextBooking());
        assertEquals(bookings.get(6).getId(), item3.getNextBooking().getId());

        assertNull(item4.getLastBooking());
        assertNull(item4.getNextBooking());

        assertEquals(2, item1.getComments().size());
        assertEquals(Set.of(comments.get(0).getId(), comments.get(2).getId()),
                item1.getComments().stream().map(CommentDto::getId).collect(Collectors.toSet()));

        assertEquals(1, item2.getComments().size());
        assertEquals(comments.get(1).getId(), item2.getComments().get(0).getId());

        assertTrue(item3.getComments().isEmpty());

        assertTrue(item4.getComments().isEmpty());
    }

    @Test
    void createItem() {
        UserDto requester = userService.createUser(new UserDto("requester", "requester@mail.com"));
        ItemRequestDto request = itemRequestService.createItemRequest(
                new ItemRequestDto("desc", null), requester.getId(), LocalDateTime.now()
        );
        UserDto owner = userService.createUser(new UserDto("owner", "owner@mail.com"));

        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> itemService.createItem(new ItemDto("item", "desc", true, null, null, 9999L), owner.getId()));
        assertEquals("Item request with id = 9999 not found", objectNotFoundException.getMessage());

        ItemDto itemNotByRequest = itemService.createItem(
                new ItemDto("item1", "desc1", true, null, null, null), owner.getId()
        );
        assertNull(itemNotByRequest.getRequestId());

        ItemDto itemByRequest = itemService.createItem(
                new ItemDto("item2", "desc2", true, null, null, request.getId()), owner.getId()
        );
        assertNotNull(itemByRequest.getRequestId());
        assertEquals(request.getId(), itemByRequest.getRequestId());
    }

    @Test
    void updateItem() {
        UserDto requester = userService.createUser(new UserDto("requester", "requester@mail.com"));
        ItemRequestDto firstRequest = itemRequestService.createItemRequest(
                new ItemRequestDto("desc1", null), requester.getId(), LocalDateTime.now()
        );
        ItemRequestDto secondRequest = itemRequestService.createItemRequest(
                new ItemRequestDto("desc2", null), requester.getId(), LocalDateTime.now()
        );
        UserDto owner = userService.createUser(new UserDto("owner", "owner@mail.com"));
        ItemDto itemNotByRequest = itemService.createItem(
                new ItemDto("item1", "desc1", true, null, null, null), owner.getId()
        );
        ItemDto itemByFirstRequest = itemService.createItem(
                new ItemDto("item2", "desc2", true, null, null, firstRequest.getId()), owner.getId()
        );

        DataAccessException dataAccessException = assertThrows(DataAccessException.class,
                () -> itemService.updateItem(new ItemDto("updatedName1", "updatedDesc1", true, null, null, null),
                        itemNotByRequest.getId(), requester.getId()));
        assertEquals("Can not update someone else's item", dataAccessException.getMessage());

        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> itemService.updateItem(new ItemDto("updatedName2", "updatedDesc2", true, null, null, 9999L),
                        itemNotByRequest.getId(), owner.getId()));
        assertEquals("Item request with id = 9999 not found", objectNotFoundException.getMessage());

        ItemDto nowItemByFirstRequest = itemService.updateItem(
                new ItemDto("updatedName3", "updatedDesc3", true, null, null, firstRequest.getId()),
                itemNotByRequest.getId(), owner.getId()
        );
        assertNotNull(nowItemByFirstRequest.getRequestId());
        assertEquals(firstRequest.getId(), nowItemByFirstRequest.getRequestId());

        ItemDto nowItemBySecondRequest = itemService.updateItem(
                new ItemDto("updatedName5", "updatedDesc5", true, null, null, secondRequest.getId()),
                itemByFirstRequest.getId(), owner.getId()
        );
        assertNotNull(nowItemBySecondRequest.getRequestId());
        assertEquals(secondRequest.getId(), nowItemBySecondRequest.getRequestId());
    }

    @Test
    void deleteItemById() {
        UserDto owner = userService.createUser(new UserDto("owner", "owner@mail.com"));
        UserDto notOwner = userService.createUser(new UserDto("notOwner", "notOwner@mail.com"));
        ItemDto item = itemService.createItem(
                new ItemDto("item1", "desc1", true, null, null, null), owner.getId()
        );

        DataAccessException dataAccessException = assertThrows(DataAccessException.class,
                () -> itemService.deleteItemById(item.getId(), notOwner.getId()));
        assertEquals("Can not delete someone else's item", dataAccessException.getMessage());
    }

    @Test
    void createComment() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        UserDto owner = userService.createUser(new UserDto("user1", "email1@mail.com"));
        UserDto commentAuthor = userService.createUser(new UserDto("user2", "email2@mail.com"));

        ItemDto item = itemService.createItem(new ItemDto("name1", "desc1", true, null, null, null), owner.getId());

        PostingCommentWithoutCompletedBookingException postingCommentException = assertThrows(
                PostingCommentWithoutCompletedBookingException.class,
                () -> itemService.createComment(new CommentDto("comment without booking", null, null),
                        item.getId(), commentAuthor.getId(), now)
        );
        assertEquals("Can not comment on item that has never been used by user",
                postingCommentException.getMessage());

        SentBookingDto bookingWithWaitingStatus = bookingService.createBooking(item.getId(), commentAuthor.getId(),
                List.of(now.minusDays(2), now.minusDays(1)));

        postingCommentException = assertThrows(PostingCommentWithoutCompletedBookingException.class,
                () -> itemService.createComment(new CommentDto("comment with waiting booking", null, null),
                        item.getId(), commentAuthor.getId(), now)
        );
        assertEquals("Can not comment on item that has never been used by user",
                postingCommentException.getMessage());

        SentBookingDto bookingWithRejectedStatus = bookingService.updateBookingStatus(
                bookingWithWaitingStatus.getId(), owner.getId(), false
        );

        postingCommentException = assertThrows(PostingCommentWithoutCompletedBookingException.class,
                () -> itemService.createComment(new CommentDto("comment with rejected booking", null, null),
                        item.getId(), commentAuthor.getId(), now)
        );
        assertEquals("Can not comment on item that has never been used by user",
                postingCommentException.getMessage());

        SentBookingDto approvedBookingInFuture = bookingService.createBooking(item.getId(), commentAuthor.getId(),
                List.of(now.plusDays(1), now.plusDays(2)));
        approvedBookingInFuture = bookingService.updateBookingStatus(
                approvedBookingInFuture.getId(), owner.getId(), true
        );
        postingCommentException = assertThrows(PostingCommentWithoutCompletedBookingException.class,
                () -> itemService.createComment(new CommentDto("comment with approved booking in future", null, null),
                        item.getId(), commentAuthor.getId(), now)
        );
        assertEquals("Can not comment on item that has never been used by user",
                postingCommentException.getMessage());

        SentBookingDto approvedBookingInPast = bookingService.createBooking(item.getId(), commentAuthor.getId(),
                List.of(now.minusDays(2), now.minusDays(1)));
        approvedBookingInPast = bookingService.updateBookingStatus(
                approvedBookingInPast.getId(), owner.getId(), true
        );

        CommentDto comment = itemService.createComment(new CommentDto("successful comment", null, null),
                item.getId(), commentAuthor.getId(), now);
        ItemDto foundItem = itemService.getItemById(item.getId(), owner.getId());

        assertNotNull(comment.getId());
        assertEquals(comment.getId(), foundItem.getComments().get(0).getId());

        assertNotNull(comment.getText());
        assertEquals("successful comment", comment.getText());

        assertNotNull(comment.getAuthorName());
        assertEquals(commentAuthor.getName(), comment.getAuthorName());

        assertNotNull(comment.getCreated());
        assertEquals(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                comment.getCreated());
    }
}