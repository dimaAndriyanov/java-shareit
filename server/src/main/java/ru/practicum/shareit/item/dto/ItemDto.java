package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingInfo;

import java.util.ArrayList;
import java.util.List;

@Data
public class ItemDto {
    private Long id;
    private final String name;
    private final String description;
    private final Boolean available;
    private final BookingInfo lastBooking;
    private final BookingInfo nextBooking;
    private final List<CommentDto> comments = new ArrayList<>();
    private final Long requestId;

    public void addCommentDto(CommentDto commentDto) {
        comments.add(commentDto);
    }
}