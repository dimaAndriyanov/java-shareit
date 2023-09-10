package ru.practicum.shareit.booking;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.SentBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.QBooking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BookingDatesIntersectWithAlreadyExistingBookingException;
import ru.practicum.shareit.exception.CanNotUpdateBookingStatusException;
import ru.practicum.shareit.exception.NotAvailableItemException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.booking.BookingMapper.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    @Qualifier("itemRepositoryDbImpl")
    private final ItemRepository itemRepository;
    @Qualifier("userRepositoryDbImpl")
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public SentBookingDto getBookingByIdAndUserId(Long id, Long userId) {
        userRepository.checkForPresenceById(userId);
        Optional<Booking> result = bookingRepository.findByIdAndOwnerOrBookerId(id, userId);
        if (result.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Booking with id = %s not found", id));
        }
        return toBookingDto(result.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SentBookingDto> getBookingsByStateAndBookerId(
            BookingState bookingState, Long bookerId, Integer from, Integer size
    ) {
        userRepository.checkForPresenceById(bookerId);
        BooleanExpression byBookerId = QBooking.booking.booker.id.eq(bookerId);
        BooleanExpression byBookerIdAndState = addBookingStateFilter(byBookerId, bookingState);
        PageRequest page = PageRequest.of(from/size, size, Sort.by(Sort.Direction.DESC, "start"));
        return toBookingDto(bookingRepository.findAll(byBookerIdAndState, page).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SentBookingDto> getBookingsByStateAndOwnerId(
            BookingState bookingState, Long ownerId, Integer from, Integer size
    ) {
        userRepository.checkForPresenceById(ownerId);
        BooleanExpression byOwnerId = QBooking.booking.item.owner.id.eq(ownerId);
        BooleanExpression byOwnerIdAndState = addBookingStateFilter(byOwnerId, bookingState);
        PageRequest page = PageRequest.of(from/size, size, Sort.by(Sort.Direction.DESC, "start"));
        return toBookingDto(bookingRepository.findAll(byOwnerIdAndState, page).getContent());
    }

    @Override
    @Transactional
    public SentBookingDto createBooking(Long itemId, Long bookerId, List<LocalDateTime> dates) {
        userRepository.checkForPresenceById(bookerId);
        User booker = userRepository.getById(bookerId);
        itemRepository.checkForPresenceById(itemId);
        Item item = itemRepository.getById(itemId);
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ObjectNotFoundException("Can not book own item");
        }
        if (!item.getAvailable()) {
            throw new NotAvailableItemException("Can not book not available item");
        }
        List<Booking> bookings = bookingRepository.findAllByItemIdOrderByStart(itemId);
        Optional<Booking> optionalIntersectingBooking = bookings.stream()
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .filter(booking ->
                        !(booking.getEnd().isBefore(dates.get(0)) || dates.get(1).isBefore(booking.getStart())))
                .findFirst();
        if (optionalIntersectingBooking.isPresent()) {
            throw new BookingDatesIntersectWithAlreadyExistingBookingException(
                    String.format("Item with id = %s is already booked from %s, till %s",
                            itemId,
                            optionalIntersectingBooking.get().getStart(),
                            optionalIntersectingBooking.get().getEnd())
            );
        }
        Booking result = bookingRepository.save(toBooking(
                dates.get(0),
                dates.get(1),
                BookingStatus.WAITING,
                booker,
                item));
        log.info("Booking with id = {} has been created", result.getId());
        return toBookingDto(result);
    }

    @Override
    @Transactional
    public SentBookingDto updateBookingStatus(Long id, Long userId, Boolean approved) {
        userRepository.checkForPresenceById(userId);
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Booking with id = %s not found", id));
        }
        if (booking.get().getBooker().getId().equals(userId)) {
            throw new ObjectNotFoundException("Can not update status of own booking");
        }
        if (!booking.get().getItem().getOwner().getId().equals(userId)) {
            throw new CanNotUpdateBookingStatusException("Can not update booking status if user is not item owner");
        }
        if (booking.get().getStatus() == BookingStatus.APPROVED) {
            throw new CanNotUpdateBookingStatusException("Can not update status of already approved booking");
        }
        if (approved) {
            booking.get().setStatus(BookingStatus.APPROVED);
        } else {
            booking.get().setStatus(BookingStatus.REJECTED);
        }
        log.info("Status of booking with id {} has ben set to {}", id, booking.get().getStatus());
        return toBookingDto(booking.get());
    }

    private BooleanExpression addBookingStateFilter(BooleanExpression byUserId, BookingState bookingState) {
        BooleanExpression result = byUserId;
        switch (bookingState) {
            case ALL :
                break;
            case WAITING :
                result = result.and(QBooking.booking.status.eq(BookingStatus.WAITING));
                break;
            case REJECTED :
                result = result.and(QBooking.booking.status.eq(BookingStatus.REJECTED));
                break;
            case CURRENT :
                result = result
                        .and(QBooking.booking.start.before(LocalDateTime.now()))
                        .and(QBooking.booking.end.after(LocalDateTime.now()));
                break;
            case PAST :
                result = result.and(QBooking.booking.end.before(LocalDateTime.now()));
                break;
            case FUTURE :
                result = result.and(QBooking.booking.start.after(LocalDateTime.now()));
        }
        return result;
    }
}