package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.SentBookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    SentBookingDto getBookingByIdAndUserId(Long id, Long userId);

    List<SentBookingDto> getBookingsByStateAndBookerId(
            BookingState bookingState, Long bookerId, Integer from, Integer size
    );

    List<SentBookingDto> getBookingsByStateAndOwnerId(
            BookingState bookingState, Long ownerId, Integer from, Integer size
    );

    SentBookingDto createBooking(Long itemId, Long bookerId, List<LocalDateTime> dates);

    SentBookingDto updateBookingStatus(Long id, Long userId, Boolean approved);
}