package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDtoForBooking;
import ru.practicum.shareit.user.dto.UserDtoForBooking;

@Data
public class SentBookingDto {
    private final Long id;
    private final String start;
    private final String end;
    private final BookingStatus status;
    private final UserDtoForBooking booker;
    private final ItemDtoForBooking item;
}