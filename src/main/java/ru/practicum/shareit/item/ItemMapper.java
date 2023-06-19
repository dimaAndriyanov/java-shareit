package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
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
        return items.stream().map(this::toItemDto).collect(Collectors.toList());
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