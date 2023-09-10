package ru.practicum.shareit.booking;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.QBooking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemRepositoryDbInterface;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepositoryDbInterface;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingRepositoryTest {
    private final BookingRepository bookingRepository;

    private final UserRepositoryDbInterface userRepository;

    private final ItemRepositoryDbInterface itemRepository;

    private final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    User saveUser(String name, String email) {
        return userRepository.save(new User(name, email));
    }

    Item saveItem(String name, String description, User owner) {
        return itemRepository.save(new Item(name, description, true, owner, null));
    }

    Booking saveBooking(LocalDateTime start, LocalDateTime end, User booker, Item item, BookingStatus status) {
        Booking booking = new Booking(start, end, booker, item);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    BooleanExpression byBookerId(Long bookerId) {
        return QBooking.booking.booker.id.eq(bookerId);
    }

    BooleanExpression byOwnerId(Long ownerId) {
        return QBooking.booking.item.owner.id.eq(ownerId);
    }

    BooleanExpression byWaiting() {
        return QBooking.booking.status.eq(BookingStatus.WAITING);
    }

    BooleanExpression byRejected() {
        return QBooking.booking.status.eq(BookingStatus.REJECTED);
    }

    BooleanExpression byCurrent() {
        return QBooking.booking.start.before(LocalDateTime.now())
                .and(QBooking.booking.end.after(LocalDateTime.now()));
    }

    BooleanExpression byPast() {
        return QBooking.booking.end.before(LocalDateTime.now());
    }

    BooleanExpression byFuture() {
        return QBooking.booking.start.after(LocalDateTime.now());
    }

    @Test
    void save() {
        User owner = saveUser("ownerName", "ownerEmail");
        Item item = saveItem("itemName", "itemDescription", owner);
        User booker = saveUser("bookerName", "bookerEmail");

        Booking booking = new Booking(now.plusHours(12), now.plusDays(1), booker, item);
        booking.setStatus(BookingStatus.WAITING);

        assertThat(booking.getId(), nullValue());

        Booking savedBooking = bookingRepository.save(booking);

        assertThat(booking.getId(), not(nullValue()));

        assertThat(savedBooking, is(booking));
    }

    @Test
    void findByIdAndOwnerOrBookerId() {
        User owner = saveUser("ownerName", "ownerEmail");
        Item item = saveItem("itemName", "itemDescription", owner);
        User booker = saveUser("bookerName", "bookerEmail");
        User otherBooker = saveUser("otherBookerName", "otherBookerEmail");
        User notBooker = saveUser("notBookerName", "notBookerEmail");
        Booking booking = saveBooking(now.plusHours(1), now.plusHours(2), booker, item, BookingStatus.WAITING);
        Booking otherBooking = saveBooking(now.plusDays(1), now.plusDays(2), otherBooker, item, BookingStatus.WAITING);

        Optional<Booking> foundBooking = bookingRepository.findByIdAndOwnerOrBookerId(booking.getId(), owner.getId());
        assertTrue(foundBooking.isPresent());
        assertThat(foundBooking.get(), is(booking));

        foundBooking = bookingRepository.findByIdAndOwnerOrBookerId(booking.getId(), booker.getId());
        assertTrue(foundBooking.isPresent());
        assertThat(foundBooking.get(), is(booking));

        foundBooking = bookingRepository.findByIdAndOwnerOrBookerId(otherBooking.getId(), owner.getId());
        assertTrue(foundBooking.isPresent());
        assertThat(foundBooking.get(), is(otherBooking));

        foundBooking = bookingRepository.findByIdAndOwnerOrBookerId(otherBooking.getId(), otherBooker.getId());
        assertTrue(foundBooking.isPresent());
        assertThat(foundBooking.get(), is(otherBooking));

        foundBooking = bookingRepository.findByIdAndOwnerOrBookerId(booking.getId(), otherBooker.getId());
        assertTrue(foundBooking.isEmpty());

        foundBooking = bookingRepository.findByIdAndOwnerOrBookerId(booking.getId(), notBooker.getId());
        assertTrue(foundBooking.isEmpty());
    }

    @Test
    void findAllByItemIdOrderByStart() {
        User owner = saveUser("userName", "userEmail");
        Item itemWithTwoBookings = saveItem("firstItemName", "firstItemDescription", owner);
        Item itemWithOneBooking = saveItem("secondItemName", "secondItemDescription", owner);
        Item itemWithoutBookings = saveItem("thirdItemName", "thirdItemDescription", owner);
        User booker = saveUser("bookerName", "bookerEmail");
        Booking firstBooking = saveBooking(now.plusHours(5), now.plusHours(6), booker, itemWithTwoBookings,
                BookingStatus.WAITING);
        Booking secondBooking = saveBooking(now.plusHours(3), now.plusHours(4), booker, itemWithTwoBookings,
                BookingStatus.WAITING);
        Booking thirdBooking = saveBooking(now.plusHours(1), now.plusHours(2), booker, itemWithOneBooking,
                BookingStatus.WAITING);

        List<Booking> foundBookings = bookingRepository.findAllByItemIdOrderByStart(itemWithTwoBookings.getId());
        assertThat(foundBookings, hasSize(2));
        assertThat(foundBookings.get(0), is(secondBooking));
        assertThat(foundBookings.get(1), is(firstBooking));

        foundBookings = bookingRepository.findAllByItemIdOrderByStart(itemWithOneBooking.getId());
        assertThat(foundBookings, hasSize(1));
        assertThat(foundBookings.get(0), is(thirdBooking));

        foundBookings = bookingRepository.findAllByItemIdOrderByStart(itemWithoutBookings.getId());
        assertThat(foundBookings, hasSize(0));

        foundBookings = bookingRepository.findAllByItemIdOrderByStart(
                List.of(
                        itemWithTwoBookings.getId(), itemWithOneBooking.getId(), itemWithoutBookings.getId()
                )
        );
        assertThat(foundBookings, hasSize(3));
        assertThat(foundBookings.get(0), is(thirdBooking));
        assertThat(foundBookings.get(1), is(secondBooking));
        assertThat(foundBookings.get(2), is(firstBooking));

        foundBookings = bookingRepository.findAllByItemIdOrderByStart(
                List.of(
                        itemWithTwoBookings.getId(), itemWithOneBooking.getId()
                )
        );
        assertThat(foundBookings, hasSize(3));
        assertThat(foundBookings.get(0), is(thirdBooking));
        assertThat(foundBookings.get(1), is(secondBooking));
        assertThat(foundBookings.get(2), is(firstBooking));

        foundBookings = bookingRepository.findAllByItemIdOrderByStart(
                List.of(
                        itemWithTwoBookings.getId()
                )
        );
        assertThat(foundBookings, hasSize(2));
        assertThat(foundBookings.get(0), is(secondBooking));
        assertThat(foundBookings.get(1), is(firstBooking));

        foundBookings = bookingRepository.findAllByItemIdOrderByStart(
                List.of(
                        itemWithOneBooking.getId()
                )
        );
        assertThat(foundBookings, hasSize(1));
        assertThat(foundBookings.get(0), is(thirdBooking));

        foundBookings = bookingRepository.findAllByItemIdOrderByStart(
                List.of(
                        itemWithoutBookings.getId()
                )
        );
        assertThat(foundBookings, hasSize(0));

        foundBookings = bookingRepository.findAllByItemIdOrderByStart(
                List.of()
        );
        assertThat(foundBookings, hasSize(0));

    }

    @Test
    void findAll() {
        User owner = saveUser("ownerName", "ownerEmail");
        User otherOwner = saveUser("otherOwnerName", "otherOwnerEmail");
        User notOwner = saveUser("notOwnerName", "notOwnerEmail");
        User booker = saveUser("bookerName", "bookerEmail");
        User otherBooker = saveUser("otherBookerName", "otherBookerEmail");
        User notBooker = saveUser("notBookerName", "notBookerEmail");
        Item item = saveItem("itemName", "itemDescription", owner);
        Item otherItem = saveItem("otherItemName", "otherItemDescription", otherOwner);

        Booking rejectedBookingInPast = saveBooking(now.minusDays(2), now.minusDays(1), booker, item,
                BookingStatus.REJECTED);
        Booking currentWaitingBooking = saveBooking(now.minusHours(1), now.plusHours(1), booker, item,
                BookingStatus.WAITING);
        Booking approvedBookingInFuture = saveBooking(now.plusDays(1), now.plusDays(2), booker, item,
                BookingStatus.APPROVED);
        Booking otherBooking = saveBooking(now.plusDays(10), now.plusDays(20), otherBooker, otherItem,
                BookingStatus.WAITING);

        PageRequest page = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> foundBookings = bookingRepository.findAll(byBookerId(booker.getId()), page).getContent();
        assertThat(foundBookings, hasSize(3));
        assertThat(foundBookings.get(0), is(approvedBookingInFuture));
        assertThat(foundBookings.get(1), is(currentWaitingBooking));
        assertThat(foundBookings.get(2), is(rejectedBookingInPast));

        foundBookings = bookingRepository.findAll(byBookerId(otherBooker.getId()), page).getContent();
        assertThat(foundBookings, hasSize(1));
        assertThat(foundBookings.get(0), is(otherBooking));

        foundBookings = bookingRepository.findAll(byBookerId(notBooker.getId()), page).getContent();
        assertThat(foundBookings, hasSize(0));

        foundBookings = bookingRepository.findAll(byOwnerId(owner.getId()), page).getContent();
        assertThat(foundBookings, hasSize(3));
        assertThat(foundBookings.get(0), is(approvedBookingInFuture));
        assertThat(foundBookings.get(1), is(currentWaitingBooking));
        assertThat(foundBookings.get(2), is(rejectedBookingInPast));

        foundBookings = bookingRepository.findAll(byOwnerId(otherOwner.getId()), page).getContent();
        assertThat(foundBookings, hasSize(1));
        assertThat(foundBookings.get(0), is(otherBooking));

        foundBookings = bookingRepository.findAll(byOwnerId(notOwner.getId()), page).getContent();
        assertThat(foundBookings, hasSize(0));

        foundBookings = bookingRepository.findAll(byWaiting(), page).getContent();
        assertThat(foundBookings, hasSize(2));
        assertThat(foundBookings.get(0), is(otherBooking));
        assertThat(foundBookings.get(1), is(currentWaitingBooking));

        foundBookings = bookingRepository.findAll(byRejected(), page).getContent();
        assertThat(foundBookings, hasSize(1));
        assertThat(foundBookings.get(0), is(rejectedBookingInPast));

        foundBookings = bookingRepository.findAll(byCurrent(), page).getContent();
        assertThat(foundBookings, hasSize(1));
        assertThat(foundBookings.get(0), is(currentWaitingBooking));

        foundBookings = bookingRepository.findAll(byPast(), page).getContent();
        assertThat(foundBookings, hasSize(1));
        assertThat(foundBookings.get(0), is(rejectedBookingInPast));

        foundBookings = bookingRepository.findAll(byFuture(), page).getContent();
        assertThat(foundBookings, hasSize(2));
        assertThat(foundBookings.get(0), is(otherBooking));
        assertThat(foundBookings.get(1), is(approvedBookingInFuture));

        foundBookings = bookingRepository.findAll(byBookerId(booker.getId()).and(byCurrent()), page).getContent();
        assertThat(foundBookings, hasSize(1));
        assertThat(foundBookings.get(0), is(currentWaitingBooking));

        foundBookings = bookingRepository.findAll(byBookerId(otherBooker.getId()).and(byCurrent()), page).getContent();
        assertThat(foundBookings, hasSize(0));
    }
}