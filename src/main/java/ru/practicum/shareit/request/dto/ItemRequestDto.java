package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class ItemRequestDto {
    private Long id;
    private final String description;
    private final LocalDateTime created;
    private final Set<ItemDto> items = new HashSet<>();

    public void addItemDto(ItemDto itemDto) {
        items.add(itemDto);
    }
}