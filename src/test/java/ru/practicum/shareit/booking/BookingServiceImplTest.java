package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.SentBookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BookingDatesIntersectWithAlreadyExistingBookingException;
import ru.practicum.shareit.exception.CanNotUpdateBookingStatusException;
import ru.practicum.shareit.exception.NotAvailableItemException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
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
class BookingServiceImplTest {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    @Autowired
    BookingServiceImplTest(ItemService itemService, UserService userService, BookingService bookingService) {
        this.itemService = itemService;
        this.userService = userService;
        this.bookingService = bookingService;
    }

    List<UserDto> setUsers() {
        return List.of(
                userService.createUser(new UserDto("user1", "email1@mail.com")),
                userService.createUser(new UserDto("user2", "email2@mail.com")),
                userService.createUser(new UserDto("user3", "email3@mail.com"))
        );
    }

    ItemDto setItem(String name, String description, Boolean available, Long ownerId) {
        return itemService.createItem(new ItemDto(name, description, available, null, null, null), ownerId);
    }

    SentBookingDto setBooking(Long itemId, Long userId, List<LocalDateTime> dates) {
        return bookingService.createBooking(itemId, userId, dates);
    }

    @Test
    void getBookingByIdAndUserId() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<UserDto> users = setUsers();
        ItemDto item1 = setItem("name1", "descr1", true, users.get(0).getId());
        SentBookingDto booking =
                setBooking(item1.getId(), users.get(1).getId(), List.of(now.plusHours(1), now.plusDays(1)));

        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBookingByIdAndUserId(booking.getId(), 9999L));
        assertEquals("User with id = 9999 not found", objectNotFoundException.getMessage());

        objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBookingByIdAndUserId(9999L, users.get(0).getId()));
        assertEquals("Booking with id = 9999 not found", objectNotFoundException.getMessage());

        objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBookingByIdAndUserId(booking.getId(), users.get(2).getId()));
        assertEquals("Booking with id = " + booking.getId() + " not found", objectNotFoundException.getMessage());

        assertEquals(booking.getId(),
                bookingService.getBookingByIdAndUserId(booking.getId(), users.get(0).getId()).getId());
        assertEquals(booking.getId(),
                bookingService.getBookingByIdAndUserId(booking.getId(), users.get(1).getId()).getId());
    }

    @Test
    void getBookingsByStateAndBookerId() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<UserDto> users = setUsers();
        ItemDto item1 = setItem("name1", "descr1", true, users.get(0).getId());
        ItemDto item2 = setItem("name2", "descr2", true, users.get(0).getId());
        ItemDto item3 = setItem("name3", "descr3", true, users.get(1).getId());
        SentBookingDto booking1 =
                setBooking(item1.getId(), users.get(1).getId(), List.of(now.plusHours(1), now.plusDays(1)));
        SentBookingDto booking2 =
                setBooking(item2.getId(), users.get(2).getId(), List.of(now.minusDays(1), now.minusHours(1)));
        SentBookingDto booking3 =
                setBooking(item3.getId(), users.get(2).getId(), List.of(now.minusHours(1), now.plusHours(1)));
        booking2 = bookingService.updateBookingStatus(booking2.getId(), users.get(0).getId(), false);
        booking3 = bookingService.updateBookingStatus(booking3.getId(), users.get(1).getId(), true);

        List<SentBookingDto> foundBookings =
                bookingService.getBookingsByStateAndBookerId(BookingState.ALL, users.get(0).getId(), 0, 10);
        assertTrue(foundBookings.isEmpty());

        foundBookings = bookingService.getBookingsByStateAndBookerId(BookingState.ALL, users.get(1).getId(), 0, 10);
        assertEquals(1, foundBookings.size());
        assertEquals(booking1.getId(), foundBookings.get(0).getId());

        foundBookings = bookingService.getBookingsByStateAndBookerId(BookingState.ALL, users.get(2).getId(), 0, 10);
        assertEquals(2, foundBookings.size());
        assertEquals(Set.of(booking2.getId(), booking3.getId()),
                foundBookings.stream().map(SentBookingDto::getId).collect(Collectors.toSet()));

        foundBookings = bookingService.getBookingsByStateAndBookerId(BookingState.REJECTED,
                users.get(2).getId(), 0, 10);
        assertEquals(1, foundBookings.size());
        assertEquals(booking2.getId(), foundBookings.get(0).getId());

        foundBookings = bookingService.getBookingsByStateAndBookerId(BookingState.PAST, users.get(2).getId(), 0, 10);
        assertEquals(1, foundBookings.size());
        assertEquals(booking2.getId(), foundBookings.get(0).getId());

        foundBookings = bookingService.getBookingsByStateAndBookerId(BookingState.CURRENT, users.get(2).getId(), 0, 10);
        assertEquals(1, foundBookings.size());
        assertEquals(booking3.getId(), foundBookings.get(0).getId());

        foundBookings = bookingService.getBookingsByStateAndBookerId(BookingState.FUTURE, users.get(2).getId(), 0, 10);
        assertTrue(foundBookings.isEmpty());
    }

    @Test
    void getBookingsByStateAndBookerIdPageable() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<UserDto> users = setUsers();
        ItemDto item = setItem("name1", "descr1", true, users.get(0).getId());
        SentBookingDto booking1 =
                setBooking(item.getId(), users.get(1).getId(), List.of(now.minusDays(2), now.minusDays(1)));
        SentBookingDto booking2 =
                setBooking(item.getId(), users.get(1).getId(), List.of(now.minusHours(1), now.plusHours(1)));
        SentBookingDto booking3 =
                setBooking(item.getId(), users.get(1).getId(), List.of(now.plusDays(1), now.plusDays(2)));

        List<SentBookingDto> foundBookingsByPage = bookingService.getBookingsByStateAndBookerId(
                BookingState.ALL, users.get(1).getId(), 0, 1
        );
        assertEquals(1, foundBookingsByPage.size());
        assertEquals(booking3, foundBookingsByPage.get(0));

        foundBookingsByPage = bookingService.getBookingsByStateAndBookerId(
                BookingState.ALL, users.get(1).getId(), 1, 2
        );
        assertEquals(2, foundBookingsByPage.size());
        assertEquals(booking3, foundBookingsByPage.get(0));
        assertEquals(booking2, foundBookingsByPage.get(1));

        foundBookingsByPage = bookingService.getBookingsByStateAndBookerId(
                BookingState.ALL, users.get(1).getId(), 2, 3
        );
        assertEquals(3, foundBookingsByPage.size());
        assertEquals(booking3, foundBookingsByPage.get(0));
        assertEquals(booking2, foundBookingsByPage.get(1));
        assertEquals(booking1, foundBookingsByPage.get(2));

        foundBookingsByPage = bookingService.getBookingsByStateAndBookerId(
                BookingState.ALL, users.get(1).getId(), 3, 3
        );
        assertTrue(foundBookingsByPage.isEmpty());
    }

    @Test
    void getBookingsByStateAndOwnerId() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<UserDto> users = setUsers();
        ItemDto item1 = setItem("name1", "descr1", true, users.get(0).getId());
        ItemDto item2 = setItem("name2", "descr2", true, users.get(0).getId());
        ItemDto item3 = setItem("name3", "descr3", true, users.get(1).getId());
        SentBookingDto booking1 =
                setBooking(item1.getId(), users.get(1).getId(), List.of(now.plusHours(1), now.plusDays(1)));
        SentBookingDto booking2 =
                setBooking(item2.getId(), users.get(2).getId(), List.of(now.minusDays(1), now.minusHours(1)));
        SentBookingDto booking3 =
                setBooking(item3.getId(), users.get(2).getId(), List.of(now.minusHours(1), now.plusHours(1)));
        booking2 = bookingService.updateBookingStatus(booking2.getId(), users.get(0).getId(), false);
        booking3 = bookingService.updateBookingStatus(booking3.getId(), users.get(1).getId(), true);

        List<SentBookingDto> foundBookings =
                bookingService.getBookingsByStateAndOwnerId(BookingState.ALL, users.get(0).getId(), 0, 10);
        assertEquals(2, foundBookings.size());
        assertEquals(Set.of(booking1.getId(), booking2.getId()),
                foundBookings.stream().map(SentBookingDto::getId).collect(Collectors.toSet()));

        foundBookings = bookingService.getBookingsByStateAndOwnerId(BookingState.FUTURE, users.get(0).getId(), 0, 10);
        assertEquals(1, foundBookings.size());
        assertEquals(booking1.getId(), foundBookings.get(0).getId());

        foundBookings = bookingService.getBookingsByStateAndOwnerId(BookingState.WAITING, users.get(0).getId(), 0, 10);
        assertEquals(1, foundBookings.size());
        assertEquals(booking1.getId(), foundBookings.get(0).getId());

        foundBookings = bookingService.getBookingsByStateAndOwnerId(BookingState.PAST, users.get(0).getId(), 0, 10);
        assertEquals(1, foundBookings.size());
        assertEquals(booking2.getId(), foundBookings.get(0).getId());

        foundBookings = bookingService.getBookingsByStateAndOwnerId(BookingState.CURRENT, users.get(0).getId(), 0, 10);
        assertTrue(foundBookings.isEmpty());

        foundBookings = bookingService.getBookingsByStateAndOwnerId(BookingState.ALL, users.get(1).getId(), 0, 10);
        assertEquals(1, foundBookings.size());
        assertEquals(booking3.getId(), foundBookings.get(0).getId());

        foundBookings = bookingService.getBookingsByStateAndOwnerId(BookingState.ALL, users.get(2).getId(), 0, 10);
        assertTrue(foundBookings.isEmpty());
    }

    @Test
    void getBookingsByStateAndOwnerIdPageable() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<UserDto> users = setUsers();
        ItemDto item = setItem("name1", "descr1", true, users.get(0).getId());
        SentBookingDto booking1 =
                setBooking(item.getId(), users.get(1).getId(), List.of(now.minusDays(2), now.minusDays(1)));
        SentBookingDto booking2 =
                setBooking(item.getId(), users.get(1).getId(), List.of(now.minusHours(1), now.plusHours(1)));
        SentBookingDto booking3 =
                setBooking(item.getId(), users.get(1).getId(), List.of(now.plusDays(1), now.plusDays(2)));

        List<SentBookingDto> foundBookingsByPage = bookingService.getBookingsByStateAndOwnerId(
                BookingState.ALL, users.get(0).getId(), 0, 1
        );
        assertEquals(1, foundBookingsByPage.size());
        assertEquals(booking3, foundBookingsByPage.get(0));

        foundBookingsByPage = bookingService.getBookingsByStateAndOwnerId(
                BookingState.ALL, users.get(0).getId(), 1, 2
        );
        assertEquals(2, foundBookingsByPage.size());
        assertEquals(booking3, foundBookingsByPage.get(0));
        assertEquals(booking2, foundBookingsByPage.get(1));

        foundBookingsByPage = bookingService.getBookingsByStateAndOwnerId(
                BookingState.ALL, users.get(0).getId(), 2, 3
        );
        assertEquals(3, foundBookingsByPage.size());
        assertEquals(booking3, foundBookingsByPage.get(0));
        assertEquals(booking2, foundBookingsByPage.get(1));
        assertEquals(booking1, foundBookingsByPage.get(2));

        foundBookingsByPage = bookingService.getBookingsByStateAndOwnerId(
                BookingState.ALL, users.get(0).getId(), 3, 3
        );
        assertTrue(foundBookingsByPage.isEmpty());
    }

    @Test
    void createBooking() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<UserDto> users = setUsers();
        ItemDto item1 = setItem("name1", "descr1", true, users.get(0).getId());
        ItemDto item2 = setItem("name2", "descr2", false, users.get(0).getId());

        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.createBooking(item1.getId(), 9999L, List.of(now.plusHours(1), now.plusHours(2))));
        assertEquals("User with id = 9999 not found", objectNotFoundException.getMessage());

        objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.createBooking(9999L, users.get(1).getId(),
                        List.of(now.plusHours(1), now.plusHours(2))));
        assertEquals("Item with id = 9999 not found", objectNotFoundException.getMessage());

        objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.createBooking(item1.getId(), users.get(0).getId(),
                        List.of(now.plusHours(1), now.plusHours(2))));
        assertEquals("Can not book own item", objectNotFoundException.getMessage());

        NotAvailableItemException notAvailableItemException = assertThrows(NotAvailableItemException.class,
                () -> bookingService.createBooking(item2.getId(), users.get(1).getId(),
                        List.of(now.plusHours(1), now.plusHours(2))));
        assertEquals("Can not book not available item", notAvailableItemException.getMessage());

        SentBookingDto successfulBooking = bookingService.createBooking(item1.getId(), users.get(1).getId(),
                List.of(now.plusHours(1), now.plusDays(1)));
        assertNotNull(successfulBooking.getId());
        assertEquals(
                bookingService.getBookingsByStateAndBookerId(BookingState.ALL,
                        users.get(1).getId(), 0, 10).get(0).getId(),
                successfulBooking.getId());
        assertNotNull(successfulBooking.getStatus());
        assertEquals(BookingStatus.WAITING, successfulBooking.getStatus());
        assertNotNull(successfulBooking.getItem());
        assertNotNull(successfulBooking.getItem().getId());
        assertEquals(item1.getId(), successfulBooking.getItem().getId());
        assertNotNull(successfulBooking.getBooker());
        assertNotNull(successfulBooking.getBooker().getId());
        assertEquals(users.get(1).getId(), successfulBooking.getBooker().getId());
        assertNotNull(successfulBooking.getStart());
        assertEquals(now.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), successfulBooking.getStart());
        assertNotNull(successfulBooking.getEnd());
        assertEquals(now.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), successfulBooking.getEnd());

        successfulBooking = bookingService.updateBookingStatus(successfulBooking.getId(), users.get(0).getId(), true);
        BookingDatesIntersectWithAlreadyExistingBookingException intersectingDatesException = assertThrows(
                BookingDatesIntersectWithAlreadyExistingBookingException.class,
                () -> bookingService.createBooking(item1.getId(), users.get(2).getId(),
                                List.of(now.plusHours(1), now.plusDays(1)))
        );
        assertEquals("Item with id = " + successfulBooking.getItem().getId() + " is already booked from " +
                now.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ", till " +
                now.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                intersectingDatesException.getMessage());
    }

    @Test
    void updateBookingStatus() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<UserDto> users = setUsers();
        ItemDto item1 = setItem("name1", "descr1", true, users.get(0).getId());
        SentBookingDto booking1 = bookingService.createBooking(item1.getId(), users.get(1).getId(),
                List.of(now.plusHours(1), now.plusHours(2)));
        SentBookingDto booking2 = bookingService.createBooking(item1.getId(), users.get(2).getId(),
                List.of(now.plusDays(1), now.plusDays(2)));

        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.updateBookingStatus(booking1.getId(), 9999L, true));
        assertEquals("User with id = 9999 not found", objectNotFoundException.getMessage());

        objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.updateBookingStatus(9999L, users.get(0).getId(), true));
        assertEquals("Booking with id = 9999 not found", objectNotFoundException.getMessage());

        objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.updateBookingStatus(booking1.getId(), users.get(1).getId(), true));
        assertEquals("Can not update status of own booking", objectNotFoundException.getMessage());

        CanNotUpdateBookingStatusException canNotUpdateBookingStatusException = assertThrows(CanNotUpdateBookingStatusException.class,
                () -> bookingService.updateBookingStatus(booking1.getId(), users.get(2).getId(), true));
        assertEquals("Can not update booking status if user is not item owner", canNotUpdateBookingStatusException.getMessage());

        SentBookingDto rejectedBooking1 =
                bookingService.updateBookingStatus(booking1.getId(), users.get(0).getId(), false);
        assertNotNull(rejectedBooking1.getStatus());
        assertEquals(BookingStatus.REJECTED, rejectedBooking1.getStatus());
        assertNotNull(rejectedBooking1.getId());
        assertEquals(booking1.getId(), rejectedBooking1.getId());
        assertNotNull(rejectedBooking1.getItem());
        assertNotNull(rejectedBooking1.getItem().getId());
        assertEquals(item1.getId(), rejectedBooking1.getItem().getId());
        assertNotNull(rejectedBooking1.getBooker());
        assertNotNull(rejectedBooking1.getBooker().getId());
        assertEquals(users.get(1).getId(), rejectedBooking1.getBooker().getId());
        assertNotNull(rejectedBooking1.getStart());
        assertEquals(now.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), rejectedBooking1.getStart());
        assertNotNull(rejectedBooking1.getEnd());
        assertEquals(now.plusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), rejectedBooking1.getEnd());

        SentBookingDto approvedBooking2 =
                bookingService.updateBookingStatus(booking2.getId(), users.get(0).getId(), true);
        assertNotNull(approvedBooking2.getStatus());
        assertEquals(BookingStatus.APPROVED, approvedBooking2.getStatus());
        assertNotNull(approvedBooking2.getId());
        assertEquals(booking2.getId(), approvedBooking2.getId());
        assertNotNull(approvedBooking2.getItem());
        assertNotNull(approvedBooking2.getItem().getId());
        assertEquals(item1.getId(), approvedBooking2.getItem().getId());
        assertNotNull(approvedBooking2.getBooker());
        assertNotNull(approvedBooking2.getBooker().getId());
        assertEquals(users.get(2).getId(), approvedBooking2.getBooker().getId());
        assertNotNull(approvedBooking2.getStart());
        assertEquals(now.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), approvedBooking2.getStart());
        assertNotNull(approvedBooking2.getEnd());
        assertEquals(now.plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), approvedBooking2.getEnd());

        canNotUpdateBookingStatusException = assertThrows(CanNotUpdateBookingStatusException.class,
                () -> bookingService.updateBookingStatus(approvedBooking2.getId(), users.get(0).getId(), false));
        assertEquals("Can not update status of already approved booking", canNotUpdateBookingStatusException.getMessage());
    }
}