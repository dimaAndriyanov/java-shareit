package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingInfo;
import ru.practicum.shareit.booking.dto.SentBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.UserMapper.*;
import static ru.practicum.shareit.item.ItemMapper.*;

@UtilityClass
public class BookingMapper {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public SentBookingDto toBookingDto(Booking booking) {
        return new SentBookingDto(
                booking.getId(),
                booking.getStart().format(formatter),
                booking.getEnd().format(formatter),
                booking.getStatus(),
                toUserDtoForBooking(booking.getBooker()),
                toItemDtoForBooking(booking.getItem())
        );
    }

    public List<SentBookingDto> toBookingDto(List<Booking> bookings) {
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    public BookingInfo getBookingInfo(Booking booking) {
        return new BookingInfo(
                booking.getId(),
                booking.getBooker().getId()
        );
    }

    public Booking toBooking(LocalDateTime start, LocalDateTime end, BookingStatus status, User booker, Item item) {
        Booking result = new Booking(
                start,
                end,
                booker,
                item
        );
        result.setStatus(status);
        return result;
    }
}