package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.HashSet;
import java.util.Set;

@Data
public class ItemRequestDto {
    private Long id;
    private final String description;
    private String created;
    private final Set<ItemDto> items = new HashSet<>();

    public void addItemDto(ItemDto itemDto) {
        items.add(itemDto);
    }

    ItemRequestDto() {
        id = null;
        description = null;
        created = null;
    }
}