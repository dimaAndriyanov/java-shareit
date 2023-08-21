package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.SentBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BookingDatesIntersectWithAlreadyExistingBookingException;
import ru.practicum.shareit.exception.CanNotUpdateBookingStatus;
import ru.practicum.shareit.exception.NotAvailableItemException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    public List<SentBookingDto> getBookingsByStateAndBookerId(BookingState bookingState, Long bookerId) {
        userRepository.checkForPresenceById(bookerId);
        List<Booking> bookings = bookingRepository.findAllByBookerIdOrderByStart(bookerId);
        return toBookingDto(bookings.stream().filter(getStateFilter(bookingState)).collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SentBookingDto> getBookingsByStateAndOwnerId(BookingState bookingState, Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdOrderByStart(ownerId);
        return toBookingDto(bookings.stream().filter(getStateFilter(bookingState)).collect(Collectors.toList()));
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
            throw new CanNotUpdateBookingStatus("Can not update booking status if user is not item owner");
        }
        if (booking.get().getStatus() == BookingStatus.APPROVED) {
            throw new CanNotUpdateBookingStatus("Can not update status of already approved booking");
        }
        if (approved) {
            booking.get().setStatus(BookingStatus.APPROVED);
        } else {
            booking.get().setStatus(BookingStatus.REJECTED);
        }
        log.info("Status of booking with id {} has ben set to {}", id, booking.get().getStatus());
        return toBookingDto(booking.get());
    }

    private boolean filterAll(Booking booking) {
        return true;
    }

    private boolean filterWaiting(Booking booking) {
        return booking.getStatus() == BookingStatus.WAITING;
    }

    private boolean filterRejected(Booking booking) {
        return booking.getStatus() == BookingStatus.REJECTED;
    }

    private boolean filterCurrent(Booking booking) {
        LocalDateTime now = LocalDateTime.now();
        return booking.getStart().isBefore(now) && now.isBefore(booking.getEnd());
    }

    private boolean filterPast(Booking booking) {
        return booking.getEnd().isBefore(LocalDateTime.now());
    }

    private boolean filterFuture(Booking booking) {
        return LocalDateTime.now().isBefore(booking.getStart());
    }

    private Predicate<Booking> getStateFilter(BookingState bookingState) {
        Predicate<Booking> stateFilter = null;
        switch (bookingState) {
            case ALL:
                stateFilter = this::filterAll;
                break;
            case WAITING:
                stateFilter = this::filterWaiting;
                break;
            case REJECTED:
                stateFilter = this::filterRejected;
                break;
            case CURRENT:
                stateFilter = this::filterCurrent;
                break;
            case PAST:
                stateFilter = this::filterPast;
                break;
            case FUTURE:
                stateFilter = this::filterFuture;
        }
        return stateFilter;
    }
}