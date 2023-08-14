package ru.practicum.shareit.booking.dto;

import lombok.Data;

@Data
public class ReceivedBookingDto {
    private Long itemId;
    private final String start;
    private final String end;
}