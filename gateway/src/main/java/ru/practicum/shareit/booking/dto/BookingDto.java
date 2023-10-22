package ru.practicum.shareit.booking.dto;

import lombok.Data;

@Data
public class BookingDto {
    private final Long itemId;
    private final String start;
    private final String end;
}