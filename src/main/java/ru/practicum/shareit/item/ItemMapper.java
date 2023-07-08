package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemMapper {
    public ItemDto toItemDto(Item item) {
        ItemDto result = new ItemDto(
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
        result.setId(item.getId());
        return result;
    }

    public List<ItemDto> toItemDto(List<Item> items) {
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    public Item toItem(ItemDto itemDto, User owner) {
        Item result = new Item(
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner
        );
        result.setId(itemDto.getId());
        return result;
    }
}