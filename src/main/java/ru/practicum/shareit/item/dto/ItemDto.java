package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingInfo;

@Data
public class ItemDto {
    private Long id;
    private final String name;
    private final String description;
    private final Boolean available;
    private final BookingInfo lastBooking;
    private final BookingInfo nextBooking;
}